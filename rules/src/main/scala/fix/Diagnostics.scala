package fix

import scalafix.v1._
import scala.meta._

object UnusedDiagnostic {
  case class UnusedLocal(name: String, position: Position) extends Diagnostic {
    override def message: String =
      s"Local definition '${name}' is never used"
  }
  
  case class UnusedParameter(name: String, owner: String, position: Position) extends Diagnostic {
    override def message: String =
      s"Parameter value '${name}' in method '${owner}' is never used"
  }
  
  case class UnusedPatVar(name: String, position: Position) extends Diagnostic {
    override def message: String =
      s"Pattern value '${name}' is never used: use a wildcard '_'"
  }

  case class UnusedPrivate(name: String, owner: String, position: Position) extends Diagnostic {
    // TODO: private method foo in object PrivatesObj is never used
    override def message: String =
      s"Private definition '${name}' in '${owner}' is never used"
  }

  case class UnusedImport(name: String, position: Position) extends Diagnostic {
    override def message: String =
      s"Unused import '${name}'"
  }
}
