package fix

import scalafix.v1._
import scala.meta._

import scalafix.internal.util.SymbolOps
import Symbols._

object Enrichments {

  implicit class RichPosition(pos: Position) extends AnyRef {
    def overlaps(other: Position): Boolean =
      pos.start <= other.end && other.start <= pos.end

    def contains(other: Option[Position]): Boolean =
      other match {
        case Some(other) => overlaps(other)
        case None        => false
      }
  }

  implicit class RichRange(range: Option[Position.Range]) extends AnyRef {
    def overlaps(other: Position): Boolean =
      range match {
        case Some(range) => range.overlaps(other)
        case None        => true
      }

    def overlaps(other: Option[Position]): Boolean =
      other match {
        case Some(other) => overlaps(other)
        case None        => true
      }
  }

  implicit class RichSymbol(sym: Symbol) {
    def isPackage: Boolean =
      !sym.isNone && !sym.isMulti && sym.value.last == '/'
    def isMulti: Boolean = sym.value.startsWith(";")
    def ensureNormalized: NormalizedSymbol = {
      val symbol = SymbolOps.inferTrailingDot(sym.value)
      NormalizedSymbol(SymbolOps.normalize(Symbol(symbol)))
    }
  }

  implicit class RichDefn(defn: Defn) {
    def isPrivateDef(implicit doc: SemanticDocument): Boolean = {
      defn.symbol.info.map(_.isPrivate).getOrElse(false) ||
      mods.exists(
        mod => // in case SemanticDB doesn't has access information (< 3.0.2)
          mod.is[Mod.Private] && mod
            .asInstanceOf[Mod.Private]
            .within
            .is[Name.Anonymous]
      )
    }

    private def mods: List[Mod] = defn match {
      case d: Defn.Val              => d.mods
      case d: Defn.Var              => d.mods
      case d: Defn.Given            => d.mods
      case d: Defn.Enum             => d.mods
      case d: Defn.EnumCase         => d.mods
      case d: Defn.RepeatedEnumCase => d.mods
      case d: Defn.GivenAlias       => d.mods
      case d: Defn.ExtensionGroup   => Nil
      case d: Defn.Def              => d.mods
      case d: Defn.Macro            => d.mods
      case d: Defn.Type             => d.mods
      case d: Defn.Class            => d.mods
      case d: Defn.Trait            => d.mods
      case d: Defn.Object           => d.mods
      case _ =>
        Nil
    }
  }
}
