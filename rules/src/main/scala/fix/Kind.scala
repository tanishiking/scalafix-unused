package fix

sealed abstract class Kind
object Kind {
  case object Param extends Kind
  case object Local extends Kind
  case object Import extends Kind
  case object ImportPkg extends Kind
  case object Private extends Kind
  case object Patvar extends Kind
}
