package scalafix

import scalafix.v1._
import scala.meta._

object ScalafixAccess {
  implicit class ScalafixAccessSymbol(sym: Symbol) {
    def isOverride(implicit doc: Symtab): Boolean =
      sym.info.map(_.info.overriddenSymbols.nonEmpty).getOrElse(false)
  }
}

