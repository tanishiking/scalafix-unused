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

import scala._
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
import scala.concurrent._ /* assert: Unused
       ^^^^^^^^^^^^^^^^
Unused import 'concurrent'
*/

object Imports {
  val m = max(1, 2)
}
