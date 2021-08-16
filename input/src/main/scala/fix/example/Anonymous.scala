/*
rule = Unused
 */
package fix.example
import scala.language.higherKinds

class Anonymous {
  this: Anonymous =>

  def locally[A](x: A): A = x

  def m1[T[_]] = ???
  def m2: Map[_, List[_]] = ???
  locally {
    ??? match { case _: List[_] => }
  }
  locally {
    val x: Int => Int = _ => ??? // assert: Unused
  }

  trait Foo
  val foo = new Foo {}
}
