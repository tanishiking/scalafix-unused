/*
rule = Unused
Unused.params = false
Unused.locals = false
Unused.imports = true
Unused.privates = false
Unused.patvars = false
*/
package fix

import scala.collection.mutable.HashMap /* assert: Unused
                                ^^^^^^^
Unused import 'HashMap'
*/

import scala.collection.mutable /* assert: Unused
                        ^^^^^^^
Unused import 'mutable'
*/

import scala.*
import util.Try /* assert: Unused
            ^^^
Unused import 'Try'
*/
import util.{Success => uSuccess} /* assert: Unused
             ^^^^^^^^^^^^^^^^^^^
Unused import 'Success'
*/

import scala.math.{max, min} /* assert: Unused
                        ^^^
Unused import 'min'
*/

// TODO: better warning message and position
import scala.concurrent.* /* assert: Unused
       ^^^^^^^^^^^^^^^^
Unused import 'concurrent'
*/

object Imports3 {
  val m = max(1, 2)
}


object A:
  class TC
  given tc: TC = ???
  given ti: Int = 1
  given ts: String = ""
  def f(using TC) = ???
  val v = 1

object B:
  import A.*
  // TODO: should be asserted
  import A.given
  def print =
    f(using tc)
    println(v)

object C:
  import A.*
  // TODO: should be asserted
  import A.given
  def print =
    println(v)

object D:
  // TODO: given Int should be asserted
  // TODO given String shouldn't be asserted
  import A.given Int
  import A.given String // assert: Unused
  println(ts)
