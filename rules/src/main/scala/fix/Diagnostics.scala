package fix

import scalafix.v1._
import scala.meta._

case class UnusedDiagnostic(displayName: String, kind: Kind, position: Position)
    extends Diagnostic {
  private val kindString = kind match {
    case Kind.Import    => "import"
    case Kind.ImportPkg => "import"
    case Kind.Local     => "definition"
    case Kind.Param     => "parameter"
    case Kind.Private   => "private field"
    case Kind.Patvar    => "pattern value"
  }
  override def message: String =
    s"Unused ${kindString} '${displayName}'"
}

object UnusedDiagnostic {
  def apply(unused: UnusedSymbol)(implicit doc: Symtab): UnusedDiagnostic = {
    val displayName =
      unused.sym.info.map(_.displayName).getOrElse(unused.sym.displayName)
    UnusedDiagnostic(
      displayName,
      unused.kind,
      unused.pos
    )
  }
}
