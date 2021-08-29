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
case class UnusedParam(sym: Symbol, paramTree: Tree, owner: Symbol, pos: Position) extends UnusedSymbol {
  override def toDiagnostic(implicit doc: Symtab): Diagnostic = {
    val name = sym.getDisplayName
    // TODO: should be tested by symbol.info.map(_.isGiven)
    // update scala3 https://github.com/lampepfl/dotty/pull/13239
    // and update scalameta and scalafix needed
    val paramName = if (name.nonEmpty) name else s"using ${paramTree.syntax}"
    UnusedDiagnostic.UnusedParameter(
      paramName,
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
