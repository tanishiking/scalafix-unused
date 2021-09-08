package fix

import scalafix.v1._
import scala.meta._
import collection.{mutable => m}

import metaconfig.Configured

import Symbols._
import Enrichments._
import scalafix.ScalafixAccess._

case class SymbolOccurrence(
    sym: Symbol,
    pos: Position
)

class Unused(config: UnusedConfig) extends SemanticRule("Unused") {

  def this() = this(UnusedConfig.default)

  override def withConfiguration(
    config: Configuration
  ): Configured[Rule] =
    config.conf
      .getOrElse("Unused")(this.config)
      .map { newConfig => new Unused(newConfig) }
  override def fix(
    implicit doc: SemanticDocument
  ): Patch = {
    val unusedSyms = m.HashMap[Symbol, UnusedSymbol]()

    val unusedPkgs = new m.HashMap[Symbol, m.Set[UnusedImport]]
      with m.MultiMap[Symbol, UnusedImport]
    // The key symbol should be normalized, and symbol in UnusedSymbol shouldn't be normalized
    val unusedImports =
      new m.HashMap[NormalizedSymbol, m.Set[UnusedImport]]
        with m.MultiMap[NormalizedSymbol, UnusedImport]

    // Used for store the symbols that shouldn't be reported unused
    val visited = m.Set[Symbol]()

    val occurrences = m.Set[SymbolOccurrence]()

    // Symbols that appear in synthetics section
    // such as implicit parameter application and implicit conversion
    val syntheticRef = m.Set[NormalizedSymbol]()

    doc.synthetics.foreach { tree =>
      Synthetics.foreachSymbol(tree) { sym =>
        syntheticRef += Symbol(sym).ensureNormalized
        Synthetics.Continue
      }
    }

    def register(
        unused: UnusedSymbol,
    ): Unit = {
      if (!visited.contains(unused.sym))
        unusedSyms(unused.sym) = unused
    }
    def registerImport(
      unused: UnusedImport
    ): Unit = {
      unusedImports.addBinding(unused.sym.ensureNormalized, unused)
    }
    def registerPkg(unused: UnusedImport): Unit = {
      unusedPkgs.addBinding(unused.sym.normalized, unused)
    }

    def traversePrivateFields(owner: Tree, templ: Template): Unit = {
      owner match {
        case cls: Defn.Class =>
          cls.ctor.paramss.flatten.foreach { param =>
            if (param.symbol.info.map(_.isPrivate).getOrElse(false))
              register(UnusedPrivate(param.symbol, owner.symbol, param.pos))
          }
        case _ =>
      }
      templ.stats.foreach { stat =>
        stat match {
          case defn: Defn if defn.isPrivateDef =>
            register(UnusedPrivate(defn.symbol, owner.symbol, defn.pos))
          case _ => ()
        }
      }
    }
    def traverseRefinements(owner: Tree, refinements: List[Stat]): Unit = {
      refinements.foreach { stat =>
        stat match {
          case defn: Defn =>
            if (config.privates && defn.isPrivateDef) {
              val unused = UnusedPrivate(defn.symbol, owner.symbol, defn.pos)
              register(unused)
            }
            visited += defn.symbol
          case decl: Decl =>
            visited += decl.symbol
          case _ => ()
        }
      }
    }

    def resolveUnusedImportPkg(sym: Symbol, pos: Option[Position]): Unit = {
      val normalized = sym.ensureNormalized
      var longestPrefix: Option[(Symbol, UnusedImport)] = None
      unusedPkgs.foreach { case (pkg, unuseds) =>
        val pkgPrefix = pkg.normalized.value
        unuseds.find(unused =>
          !unused.pos.contains(pos) && unused.scope.overlaps(pos)
        ) match {
          case Some(found)
              if normalized.value.startsWith(pkgPrefix) &&
                pkgPrefix.length > longestPrefix
                  .map { case (sym, _) => sym.value.length }
                  .getOrElse(0) =>
            longestPrefix = Some((pkg.normalized, found))
          case _ => ()
        }
      }
      longestPrefix match {
        case Some((key, value)) => unusedPkgs.removeBinding(key, value)
        case None               => ()
      }
    }

    doc.tree.traverse {
      case tree: Term.Function if config.params =>
        tree.params.foreach { param =>
          if (param.name.value.nonEmpty) // (_: Int) => 42 has name ''
            register(UnusedParam(param.symbol, param, tree.symbol, param.pos, tree.pos.asRange))
        }
      case tree: Defn.Def if config.params =>
        val methodName = tree.symbol.getDisplayName
        val isOverride = tree.symbol.isOverride || tree.mods.exists(mod => mod.is[Mod.Override])
        if (!config.disabledParamsOfMethods.exists(_ == methodName) && !isOverride) {
          for {
            params <- tree.paramss
            param <- params
          } yield {
            register(UnusedParam(param.symbol, param, tree.symbol, param.pos, tree.pos.asRange))
          }
        }

      case tree: Defn if tree.symbol.isLocal && config.locals =>
        register(UnusedLocal(tree.symbol, tree.pos))

      // Register unused private fields
      // e.g.
      // ```
      // case class Foo(private val x: Int)
      //                ^^^^^^^^^^^^^^^^^^
      //
      // class Foo {
      //   private val x = 1
      //   ^^^^^^^^^^^^^^^^^
      // }
      // ```
      case tree: Defn.Object if config.privates =>
        traversePrivateFields(tree, tree.templ)
      case tree: Defn.Trait if config.privates =>
        traversePrivateFields(tree, tree.templ)
      case tree: Defn.Class if config.privates =>
        traversePrivateFields(tree, tree.templ)
      case tree: Type.Refine =>
        traverseRefinements(tree, tree.stats)
      case tree: Term.NewAnonymous =>
        traverseRefinements(tree, tree.templ.stats)

      // Register unused pattern value
      // e.g.
      // ```
      // ... match {
      //   case Pat1(aaa, bbb) => println(a)
      //                  ^^^
      //   case bind @ Pat2(_) => ???
      //        ^^^^
      // }
      //
      // def foo = {
      //   val (aaa, _) = (1, 2)
      //      ^^^
      // }
      // ```
      //
      // isLoal is needed not to catch
      // ```
      // object Pat {
      //   val (a, b) = (1, 2)
      // }
      // ```
      // as unused pattern value
      case tree: Pat.Var if tree.symbol.isLocal && config.patvars =>
        register(UnusedPatVar(tree.symbol, tree.pos))

      case tree: Importer if config.imports =>
        def isDisabled(sym: Symbol): Boolean = {
          val normalized = sym.normalized.value
          config.disabledImports.exists(disable =>
            normalized.startsWith(disable)
          )
        }
        val isExport = tree.parent.map(_.is[Export]).getOrElse(false)
        if (!isExport) {
          tree.importees.foreach { importee =>
            val scope = for {
              importTree <- tree.parent
              parent <- importTree.parent
              range <- parent.pos match {
                case range: Position.Range if parent.isNot[Pkg] => Some(range)
                case _                                          => None
              }
            } yield range

            def handleWildcard(): Unit = {
              if (!isDisabled(tree.ref.symbol) && tree.ref.symbol.isPackage)
                registerPkg(
                  UnusedImport(tree.ref.symbol, tree.pos, scope))
            }

            importee match {
              // Limitation: don't warn wildcard import other than from package
              case _: Importee.Wildcard =>
                handleWildcard
              case _: Importee.GivenAll =>
                handleWildcard
              case _: Importee.Given =>
                handleWildcard
              case other if !isDisabled(other.symbol) =>
                if (other.symbol.isPackage)
                  registerPkg(
                    UnusedImport(other.symbol, other.pos, scope))
                else
                  registerImport(
                    UnusedImport(
                      other.symbol,
                      other.pos,
                      scope,
                    )
                  )
              case _ =>
            }
          }
        }

      case tree if !tree.symbol.isNone && !tree.is[Defn] =>
        val isEndmarker = tree.parent.map(_.is[Term.EndMarker]).getOrElse(false)
        if (!isEndmarker) {
          val occurrence = SymbolOccurrence(
            tree.symbol,
            tree.pos
          )
          occurrences.add(occurrence)
        }
    }

    occurrences.foreach { occurrence =>
      val sym = occurrence.sym
      unusedSyms.get(sym).foreach { defn =>
        // if defn.pos.contains(tree.pos) = true
        // that means the tree is a definition itself, and do not mark it as used
        if (
          !defn.pos.overlaps(occurrence.pos) &&
          defn.scope.map(_.overlaps(occurrence.pos)).getOrElse(true) // true if scope is None
        )
        unusedSyms.remove(sym)
      }

      val normalized = sym.ensureNormalized
      unusedImports.get(normalized) match {
        case Some(defns) =>
          defns.foreach { defn =>
            if (
              !defn.pos
                .overlaps(occurrence.pos) && defn.scope.overlaps(occurrence.pos)
            )
              unusedImports.removeBinding(normalized, defn)
          }
        case _ => ()
      }
      resolveUnusedImportPkg(sym, Some(occurrence.pos))
    }

    // Mark as used if the symbols found from synthetic section
    unusedSyms.keys.foreach { key =>
      val normalized = key.ensureNormalized
      if (syntheticRef.contains(normalized))
        unusedSyms.remove(key)
    }
    syntheticRef.foreach { ref =>
      if (unusedImports.get(ref).isDefined) unusedImports.remove(ref)
      resolveUnusedImportPkg(ref, None)
    }

    val patches1 = unusedSyms.values.map { unused =>
      Patch.lint(unused.toDiagnostic)
    }

    val patches2 = unusedImports.values.flatten.map { unused =>
      Patch.lint(unused.toDiagnostic)
    }

    val patches3 = unusedPkgs.values.flatten.map { pkg =>
      Patch.lint(pkg.toDiagnostic)
    }

    (patches1 ++ patches2 ++ patches3).asPatch
  }

}
