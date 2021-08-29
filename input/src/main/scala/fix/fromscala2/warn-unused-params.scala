/*
rule = Unused
*/
package fix.fromscala2
package params

// scalac: -Ywarn-unused:params -Xfatal-warnings
//

trait InterFace {
  /** Call something. */
  def call(a: Int, b: String, c: Double): Int
}

trait BadAPI extends InterFace {
  def f(a: Int,
        b: String, // assert: Unused
        c: Double): Int = {
    println(c)
    a
  }
  // TODO: don't warn deprecated metdho
  @deprecated ("no warn in deprecated API", since="yesterday")
  def g(a: Int,
        b: String, // assert: Unused
        c: Double): Int = {
    println(c)
    a
  }

  // TODO
  // no warn, required by superclass
  override def call(a: Int,
                    b: String, // assert: Unused
                    c: Double): Int = {
    println(c)
    a
  }

  def meth(x: Int) = x

  override def equals(other: Any): Boolean = true  // assert: Unused

  def i(implicit s: String) = 42 // assert: Unused

  /*
  def future(x: Int): Int = {
    val y = 42
    val x = y               // maybe option to warn only if shadowed
    x
  }
  */
}

// mustn't alter warnings in super
trait PoorClient extends BadAPI {
  override def meth(x: Int) = ??? // assert: Unused
  override def f(a: Int, b: String, c: Double): Int = a + b.toInt + c.toInt
}

// TODO: warn unused ctor param
class Unusing(u: Int) {
  def f = ???
}

class Valuing(val u: Int)        // no warn

class Revaluing(u: Int) { def f = u } // no warn

case class CaseyKasem(k: Int)        // no warn

case class CaseyAtTheBat(k: Int)(s: String)        // warn

trait Ignorance {
  def f(readResolve: Int) = 42 // assert: Unused
}

class Reusing(u: Int) extends Unusing(u)   // no warn

class Main {
  def main(args: Array[String]): Unit = println("hello, args") // no warn
}

trait Unimplementation {
  def f(u: Int): Int = ??? // assert: Unused
}

trait DumbStuff {
  def f(implicit dummy: DummyImplicit) = 42 // assert: Unused
  def g(dummy: DummyImplicit) = 42 // assert: Unused
}
trait Proofs {
  def f[A, B](implicit ev: A =:= B) = 42 // assert: Unused
  def g[A, B](implicit ev: A <:< B) = 42 // assert: Unused
  def f2[A, B](ev: A =:= B) = 42 // assert: Unused
  def g2[A, B](ev: A <:< B) = 42 // assert: Unused
}

trait Anonymous {
  def f = (i: Int) => 42 // assert: Unused

  def f1 = (_: Int) => 42     // no warn underscore parameter (a fresh name)

  def f2: Int => Int = _ + 1  // no warn placeholder syntax (a fresh name and synthetic parameter)

  def g = for (i <- List(1)) yield 42 // assert: Unused
}
