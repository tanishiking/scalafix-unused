/*
rule = Unused
*/
package fix.example
package flags

import scala.language.experimental.macros

package object p {
  private lazy val x = 1 // no assertion, it's package object
  protected implicit var y: Int = 2
  def z(pp: Int) = 3 // assert: Unused
  // def m[TT]: Int = macro ???
  abstract class C[+T, -U, V](x: T, y: U, z: V) {
    def this() = this(???, ???, ???)
    def w: Int
  }
  type T1 = Int
  type T2[T] = S[T]
  type U <: Int
  type V >: Int
  case object X
  final class Y
  sealed trait Z
  class AA(x: Int, val y: Int, var z: Int)
  class S[@specialized T]
  val List(xs1) = ???
  ??? match { case List(xs2) => ??? } // assert: Unused
  ??? match { case _: List[t] => ??? }
}
