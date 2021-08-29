package fix

import scalafix.v1._
import scala.meta._

import Enrichments.RichSymbol

sealed abstract class UnusedSymbol {
  val sym: Symbol
  val pos: Position
  def toDiagnostic(implicit doc: Symtab): Diagnostic
}
case class UnusedLocal(sym: Symbol, pos: Position) extends UnusedSymbol {
  override def toDiagnostic(implicit doc: Symtab): Diagnostic = {
    UnusedDiagnostic.UnusedLocal(
      sym.getDisplayName,
      pos,
    )
  }
}
case class UnusedParam(sym: Symbol, owner: Symbol, pos: Position) extends UnusedSymbol {
  override def toDiagnostic(implicit doc: Symtab): Diagnostic = {
    UnusedDiagnostic.UnusedParameter(
      sym.getDisplayName,
      owner.getDisplayName,
      pos,
    )
  }
}
case class UnusedPrivate(sym: Symbol, owner: Symbol, pos: Position) extends UnusedSymbol {
  override def toDiagnostic(implicit doc: Symtab): Diagnostic = {
    UnusedDiagnostic.UnusedPrivate(
      sym.getDisplayName,
      owner.getDisplayName,
      pos,
    )
  }
}
case class UnusedPatVar(sym: Symbol, pos: Position) extends UnusedSymbol {
  override def toDiagnostic(implicit doc: Symtab): Diagnostic = {
    UnusedDiagnostic.UnusedPatVar(
      sym.getDisplayName,
      pos,
    )
  }
}

case class UnusedImport(
  sym: Symbol,
  pos: Position,
  scope: Option[Position.Range] = None
) {
  def toDiagnostic(implicit doc: Symtab): Diagnostic = {
    UnusedDiagnostic.UnusedImport(
      sym.getDisplayName,
      pos,
    )
  }
}
