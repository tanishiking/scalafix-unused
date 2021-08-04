/*
rule = Unused
Unused.params = false
Unused.locals = false
Unused.imports = false
Unused.privates = false
Unused.patvars = true
*/

package fix

object PatVars {
  sealed trait Sealed
  case class A(x: Int) extends Sealed
  case class B(x: Int, y: Int) extends Sealed
  case class C(x: Int, y: Int) extends Sealed
}

object PatVarsPos {
  import PatVars._
  def pattern1(x: Sealed): Unit = x match {
    case A(x) => () /* assert: Unused
           ^
Unused pattern value 'x'
*/
    case B(aaa, bbb) => println(aaa) /* assert: Unused
                ^^^
Unused pattern value 'bbb'
*/
    case c @ C(x, y) => println(x + y) /* assert: Unused
         ^
Unused pattern value 'c'
*/
  }

  def patterns2 = {
    val (a, b, _) = (1, 2, 3) /* assert: Unused
            ^
Unused pattern value 'b'
    */
    println(a)
  }
}


object PatVarsNeg {
  import PatVars._
  def pattern1(x: Sealed): Unit = x match {
    case A(x) if x > 0 => ()
    case A(_) => ()
    case B(aaa, _) => println(aaa)
    case c @ C(x, y) =>
      println(x + y)
      println(c)
  }

  object Pattern2 {
    val (a, b) = (1, 2)
  }
}
