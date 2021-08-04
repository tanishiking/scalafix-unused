package fix

import scalafix.v1._

/**
 * Utilities to work with SemanticDB synthetics.
 */
object Synthetics {
  sealed abstract class ForeachResult {
    def isStop: Boolean = this == Stop
  }
  case object Continue extends ForeachResult
  case object Stop extends ForeachResult

  def foreachSymbol(
      tree: SemanticTree
  )(fn: String => ForeachResult): ForeachResult = {
    def isStop(t: SemanticTree): Boolean =
      t match {
        case ApplyTree(function, arguments) =>
          isStop(function) || arguments.exists(isStop)
        case SelectTree(_, id) =>
          isStop(id)
        case IdTree(info) =>
          fn(info.symbol.value).isStop
        case TypeApplyTree(function, _) =>
          isStop(function)
        case FunctionTree(_, body) =>
          isStop(body)
        case LiteralTree(_) =>
          false
        case MacroExpansionTree(_, _) =>
          false
        case OriginalTree(_) => false
        case _ => false
      }
    if (isStop(tree)) Stop
    else Continue
  }
}
