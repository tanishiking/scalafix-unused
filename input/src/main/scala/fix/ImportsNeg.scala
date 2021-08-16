/*
rule = Unused
Unused.params = false
Unused.locals = false
Unused.imports = true
Unused.privates = false
Unused.patvars = false
 */
package fix

import collection.mutable._
import scala.util
import scala.collection.immutable.HashSet
import scala.collection.immutable.{HashMap => HM}
import scala.math.{BigInt, BigDecimal, max, min}

object ImportsNeg {
  val map = HashMap[Int, Int]()

  val nonfatal = util.control.NonFatal(new Exception("test"))

  val immutableHashset = HashSet[Int]()
  val immutableHashmap = HM[Int, Int]()

  val bigint = BigInt(1)
  val bigdecimal = BigDecimal(1)
  val mmax = max(1, 2)
  val mmin = min(1, 2)
}
