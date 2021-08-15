/*
rule = Unused
*/
package fix.example
package privates

class Bippy(a: Int, b: Int) {
  // TODO: assert unused??
  private def this(c: Int) = this(c, c)
  private def bippy(x: Int): Int      = bippy(x) // assert: Unused
  private def boop(x: Int)            = x+a+b // assert: Unused

  // TODO
  // no warn, might have been inlined
  final private val MILLIS1           = 2000 // assert: Unused
  final private val MILLIS2: Int      = 1000 // assert: Unused
  final private val HI_COMPANION: Int = 500 // no warn, accessed from companion
  def hi() = Bippy.HI_INSTANCE
}
object Bippy {
  def hi(x: Bippy) = x.HI_COMPANION
  // no warn, accessed from instance
  private val HI_INSTANCE: Int = 500
  private val HEY_INSTANCE: Int = 1000 // assert: Unused
  private lazy val BOOL: Boolean = true // assert: Unused
}

class A(val msg: String)
class B1(msg: String) extends A(msg)
class B2(msg0: String) extends A(msg0)
class B3(msg0: String) extends A("msg")

trait Bing

trait Accessors {
  private var v1: Int = 0 // assert: Unused
  // TODO: warn, never set
  private var v2: Int = 0
  private var v3: Int = 0 // assert: Unused
  private var v4: Int = 0 // no warn

  def bippy(): Int = {
    v3 = 5
    v4 = 6
    v2 + v4
  }
}

trait DefaultArgs {
  // warn about default getters for x2 and x3
  private def bippy(x1: Int, x2: Int = 10, x3: Int = 15): Int = x1 + x2 + x3

  def boppy() = bippy(5, 100, 200)
}

/* scala/bug#7707 Both usages warn default arg because using PrivateRyan.apply, not new.
case class PrivateRyan private (ryan: Int = 42) { def f = PrivateRyan() }
object PrivateRyan { def f = PrivateRyan() }
*/

class Outer {
  class Inner
}

trait Locals {
  def f0 = {
    var x = 1 // assert: Unused
    var y = 2
    y = 3
    y + y
  }
  def f1 = {
    val a = new Outer // no warn
    val b = new Outer // assert: Unused
    new a.Inner
  }
  def f2 = {
    var x = 100 // warn about it being a var TODO
    x
  }
}

object Types {
  private object Dongo { def f = this } // assert: Unused
  private class Bar1 // assert: Unused
  private class Bar2 // no warn
  private type Alias1 = String // assert: Unused
  private type Alias2 = String // no warn
  def bippo = (new Bar2).toString

  def f(x: Alias2) = x.length

  def l1() = {
    object HiObject { def f = this } // assert: Unused
    class Hi { // assert: Unused
      def f1: Hi = new Hi
      def f2(x: Hi) = x
    }
    class DingDongDoobie // assert: Unused
    class Bippy // no warn
    type Something = Bippy // no warn
    type OtherThing = String // assert: Unused
    (new Bippy): Something
  }
}

trait Underwarn {
  def f(): Seq[Int]

  def g() = {
    val Seq(_, _) = f()  // no warn
    true
  }
}

class OtherNames {
  private def x_=(i: Int): Unit = () // assert: Unused
  private def x: Int = 42 // assert: Unused
  private def y_=(i: Int): Unit = () // assert: Unused
  private def y: Int = 42

  def f = y
}

case class C(a: Int, b: String, c: Option[String])
case class D(a: Int)

trait Boundings {

  def c = C(42, "hello", Some("world"))
  def d = D(42)

  def f() = {
    val C(x, y, Some(z)) = c // assert: Unused
    17
  }
  def g() = {
    val C(x @ _, y @ _, Some(z @ _)) = c // assert: Unused
    17
  }
  def h() = {
    val C(x @ _, y @ _, z @ Some(_)) = c // assert: Unused
    17
  }

  def v() = {
    val D(x) = d // assert: Unused
    17
  }
  def w() = {
    val D(x @ _) = d // assert: Unused
    17
  }

}

trait Forever {
  def f = {
    val t = Option((17, 42))
    for {
      ns <- t
      (i, j) = ns
    } yield (i + j)
  }
  def g = {
    val t = Option((17, 42))
    for {
      ns <- t
      (i, j) = ns // assert: Unused
    } yield 42                           // val emitted only if needed, hence nothing unused
  }
}

trait Ignorance {
  private val readResolve = 42 // assert: Unused
}

trait CaseyKasem {
  def f = 42 match {
    case x if x < 25 => "no warn"
    case y if toString.nonEmpty => "no warn" + y
    case z => "warn" // assert: Unused
  }
}
trait CaseyAtTheBat {
  def f = Option(42) match {
    case Some(x) if x < 25 => "no warn"
    case Some(y @ _) if toString.nonEmpty => "no warn" // assert: Unused
    case Some(z) => "warn" // assert: Unused
    case None => "no warn"
  }
}

class `not even using companion privates`

object `not even using companion privates` {
  private implicit class `for your eyes only`(i: Int) { // assert: Unused
    def f = i
  }
}

class `no warn in patmat anonfun isDefinedAt` {
  def f(pf: PartialFunction[String, Int]) = pf("42")
  def g = f {
    case s => s.length        // no warn (used to warn case s => true in isDefinedAt)
  }
}

// this is the ordinary case, as AnyRef is an alias of Object
class `nonprivate alias is enclosing` {
  class C
  type C2 = C
  private class D extends C2 // assert: Unused
}

object `classof something` {
}

trait `short comings` {
  def f: Int = {
    val x = 42 // assert: Unused
    17
  }
}
