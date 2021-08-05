package fix

import scalafix.v1._
import scala.meta._
import collection.mutable

import scalafix.internal.util.SymbolOps

import metaconfig.Configured
import scala.annotation.unused

case class UnusedSymbol(
    pos: Position,
    sym: Symbol,
    kind: Kind
)
class Unused(config: UnusedConfig) extends SemanticRule("Unused") {
  def this() = this(UnusedConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse("Unused")(this.config)
      .map { newConfig => new Unused(newConfig) }
  override def fix(implicit doc: SemanticDocument): Patch = {
    val unusedSyms = collection.mutable.HashMap[Symbol, UnusedSymbol]()
    val unusedPkgs = collection.mutable.HashMap[Symbol, UnusedSymbol]()

    // Symbols that appear in synthetics section
    // such as implicit parameter application and implicit conversion
    val syntheticRef = mutable.Set[String]()

    doc.synthetics.foreach { tree =>
      Synthetics.foreachSymbol(tree) { sym =>
        syntheticRef += Symbol(sym).normalized.value
        Synthetics.Continue
      }
    }

    def isPrivateDef(defn: Defn): Boolean = {
      defn.symbol.info.map(_.isPrivate).getOrElse(false)
    }

    def registerUnused(tree: Tree, kind: Kind): Unit = {
      val unused = UnusedSymbol(tree.pos, tree.symbol, kind)
      unusedSyms(unused.sym.normalized) = unused
    }
    def registerUnusedPkg(tree: Tree): Unit = {
      val unused = UnusedSymbol(tree.pos, tree.symbol, Kind.ImportPkg)
      unusedPkgs(unused.sym.normalized) = unused
    }
    def registerPrivateFields(templ: Template): Unit = {
      templ.stats.foreach { stat =>
        stat match {
          case defn: Defn
              if isPrivateDef(defn) =>
            registerUnused(defn, Kind.Private)
          case _ => ()
        }
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

      case tree: Defn.Val if tree.symbol.isLocal && config.locals =>
        registerUnused(tree, Kind.Local)
      case tree: Defn.Var if tree.symbol.isLocal && config.locals =>
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
        tree.importees.foreach { importee =>
          importee match {
            case wildcard: Importee.Wildcard =>
              registerUnusedPkg(tree.ref)
            case other =>
              if (other.symbol.isPackage)
                registerUnusedPkg(other)
              else if (
                !other.symbol.normalized.value.startsWith("scala.language.")
              )
                registerUnused(other, Kind.Import)
          }
        }

      case tree if !tree.symbol.isNone =>
        val sym = tree.symbol.normalized
        unusedSyms.get(sym) match {
          case Some(defn)
              if !defn.pos.contains(
                tree.pos
              ) =>
            // if defn.pos.contains(tree.pos) = true
            // that means the tree is a definition itself, and do not mark it as used
            unusedSyms.remove(sym)
          case _ => ()
        }

        val grandParent = tree.parent.flatMap(_.parent)
        var longestPrefix: Symbol = Symbol.None
        unusedPkgs.foreach { case (pkg, unused) =>
          val pkgPrefix = pkg.normalized.value
          if (
            sym.value.startsWith(pkgPrefix) &&
            !unused.pos.contains(tree.pos) &&
            pkgPrefix.length > longestPrefix.value.length
          ) {
            longestPrefix = pkg.normalized
          }
        }
        if (!longestPrefix.isNone) {
          unusedPkgs.remove(longestPrefix)
        }
    }

    // Mark as used if the symbols found from synthetic section
    unusedSyms.keys.foreach { sym =>
      val normalized = sym.normalized
      if (syntheticRef.contains(normalized.value))
        unusedSyms.remove(normalized)
    }

    val patches1 = unusedSyms.values.map { unused =>
      Patch.lint(UnusedDiagnostic(unused))
    }

    val patches2 = unusedPkgs.values.map { pkg =>
      Patch.lint(UnusedDiagnostic(pkg))
    }

    (patches1 ++ patches2).asPatch
  }

  private implicit class RichPosition(pos: Position) extends AnyRef {
    def contains(other: Position): Boolean =
      pos.start <= other.start && other.end <= pos.end
  }
  private implicit class RichSymbol(sym: Symbol) {
    def isPackage: Boolean =
      !sym.isNone && !sym.isMulti && sym.value.last == '/'
    def isMulti: Boolean = sym.value.startsWith(";")
    def normalized: Symbol = {
      val symbol = SymbolOps.inferTrailingDot(sym.value)
      SymbolOps.normalize(Symbol(symbol))
    }
  }
}
