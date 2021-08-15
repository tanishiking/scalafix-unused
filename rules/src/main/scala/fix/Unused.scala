package fix

import scalafix.v1._
import scala.meta._
import collection.{mutable => m}

import metaconfig.Configured
import scala.annotation.unused

case class UnusedSymbol(
    pos: Position,
    sym: Symbol,
    kind: Kind,
    scope: Option[Position.Range] = None
)

class Unused(config: UnusedConfig) extends SemanticRule("Unused") {
  import Symbols._
  import Enrichments._

  def this() = this(UnusedConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse("Unused")(this.config)
      .map { newConfig => new Unused(newConfig) }
  override def fix(implicit doc: SemanticDocument): Patch = {
    val unusedSyms = m.HashMap[Symbol, UnusedSymbol]()

    val unusedPkgs = new m.HashMap[Symbol, m.Set[UnusedSymbol]]
      with m.MultiMap[Symbol, UnusedSymbol]
    // The key symbol should be normalized, and symbol in UnusedSymbol shouldn't be normalized
    val unusedImports =
      new m.HashMap[NormalizedSymbol, m.Set[UnusedSymbol]]
        with m.MultiMap[NormalizedSymbol, UnusedSymbol]

    // Used for store the symbols that shouldn't be reported unused
    val visited = m.Set[Symbol]()

    // Symbols that appear in synthetics section
    // such as implicit parameter application and implicit conversion
    val syntheticRef = m.Set[NormalizedSymbol]()

    doc.synthetics.foreach { tree =>
      Synthetics.foreachSymbol(tree) { sym =>
        syntheticRef += Symbol(sym).ensureNormalized
        Synthetics.Continue
      }
    }

    def registerUnused(
        tree: Tree,
        kind: Kind,
        scope: Option[Position.Range] = None
    ): Unit = {
      if (!visited.contains(tree.symbol)) {
        val unused = UnusedSymbol(tree.pos, tree.symbol, kind, scope)
        if (kind == Kind.Import) {
          unusedImports.addBinding(unused.sym.ensureNormalized, unused)
        } else {
          unusedSyms(unused.sym) = unused
        }
      }
    }
    def registerUnusedPkg(tree: Tree, scope: Option[Position.Range]): Unit = {
      val unused = UnusedSymbol(tree.pos, tree.symbol, Kind.ImportPkg, scope)
      unusedPkgs.addBinding(unused.sym.normalized, unused)
    }
    def registerPrivateFields(templ: Template): Unit = {
      templ.stats.foreach { stat =>
        stat match {
          case defn: Defn if defn.isPrivateDef =>
            registerUnused(defn, Kind.Private)
          case _ => ()
        }
      }
    }
    def registerRefinements(refinements: List[Stat]): Unit = {
      refinements.foreach { stat =>
        stat match {
          case defn: Defn =>
            if (config.privates && defn.isPrivateDef)
              registerUnused(defn, Kind.Private)
            visited += defn.symbol
          case decl: Decl =>
            visited += decl.symbol
          case _ => ()
        }
      }
    }

    def resolveUnusedImportPkg(sym: Symbol, pos: Option[Position]): Unit = {
      val normalized = sym.ensureNormalized
      var longestPrefix: Option[(Symbol, UnusedSymbol)] = None
      unusedPkgs.foreach { case (pkg, unuseds) =>
        val pkgPrefix = pkg.normalized.value
        unuseds.find(unused =>
          !unused.pos.contains(pos) && unused.scope.cover(pos)
        ) match {
          case Some(found)
              if normalized.value.startsWith(pkgPrefix) &&
                pkgPrefix.length > longestPrefix.map(_._1.value.length).getOrElse(0) =>
            longestPrefix = Some((pkg.normalized, found))
          case _ => ()
        }
      }
      longestPrefix match {
        case Some((key, value)) => unusedPkgs.removeBinding(key, value)
        case None => ()
      }
    }

    doc.tree.collect {
      case tree: Defn.Def if config.params =>
        for {
          params <- tree.paramss
          param <- params
        } yield {
          registerUnused(param, Kind.Param)
        }

      case tree: Defn if tree.symbol.isLocal && config.locals =>
        registerUnused(tree, Kind.Local)

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
        registerPrivateFields(tree.templ)
      case tree: Defn.Trait if config.privates =>
        registerPrivateFields(tree.templ)
      case tree: Defn.Class if config.privates =>
        registerPrivateFields(tree.templ)
        tree.ctor.paramss.flatten.foreach { param =>
          if (param.symbol.info.map(_.isPrivate).getOrElse(false))
            registerUnused(param, Kind.Private)
        }

      case tree: Type.Refine =>
        registerRefinements(tree.stats)
      case tree: Term.NewAnonymous =>
        registerRefinements(tree.templ.stats)

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
        registerUnused(tree, Kind.Patvar)

      case tree: Importer if config.imports =>
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

            importee match {
              case wildcard: Importee.Wildcard =>
                // Limitation: don't warn wildcard import other than from package
                if (tree.ref.symbol.isPackage)
                  registerUnusedPkg(tree.ref, scope)
              case other =>
                if (other.symbol.isPackage)
                  registerUnusedPkg(other, scope)
                else if (
                  !other.symbol.normalized.value.startsWith("scala.language.")
                )
                  registerUnused(other, Kind.Import, scope)
            }
          }
        }

      case tree if !tree.symbol.isNone && !tree.is[Defn] =>
        val sym = tree.symbol
        unusedSyms.get(sym) match {
          // if defn.pos.contains(tree.pos) = true
          // that means the tree is a definition itself, and do not mark it as used
          case Some(defn) if !defn.pos.contains(tree.pos) =>
            unusedSyms.remove(sym)
          case _ => ()
        }

        val normalized = sym.ensureNormalized
        unusedImports.get(normalized) match {
          case Some(defns) =>
            defns.foreach { defn =>
              if (!defn.pos.contains(tree.pos) && defn.scope.cover(tree.pos))
                unusedImports.removeBinding(normalized, defn)
            }
          case _ => ()
        }

        resolveUnusedImportPkg(sym, Some(tree.pos))
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
      Patch.lint(UnusedDiagnostic(unused))
    }

    val patches2 = unusedImports.values.flatten.map { unused =>
      Patch.lint(UnusedDiagnostic(unused))
    }

    val patches3 = unusedPkgs.values.flatten.map { pkg =>
      Patch.lint(UnusedDiagnostic(pkg))
    }

    (patches1 ++ patches2 ++ patches3).asPatch
  }

}
