package fix

import scalafix.v1._

object Symbols {
  // TODO?: maybe we should use https://github.com/estatico/scala-newtype to prevent boxing/unboxing
  case class NormalizedSymbol(sym: Symbol)
  implicit def stripNormalized(normalized: NormalizedSymbol): Symbol =
    normalized.sym
}
