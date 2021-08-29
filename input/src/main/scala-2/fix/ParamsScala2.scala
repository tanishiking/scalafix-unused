/*
rule = Unused
Unused.params = true
Unused.locals = false
Unused.imports = false
Unused.privates = false
Unused.patvars = false
*/
package fix

object ParamsScala2 {
  object Neg {
    def implicit1(x: Int)(implicit y: Int) = x + y
    def implicit2(implicit x: Int) = implicit1(1)
  }

  trait InterFace {
    /** Call something. */
    def call(a: Int, b: String, c: Double): Int
    def call2(a: Int, b: String): Int
  }

  class BadAPI extends InterFace {
    def f(a: Int,
          b: String, // assert: Unused
          c: Double): Int = {
      println(c)
      a
    }
    override def call(
      a: Int,
      b: String, // no warn, required by superclass
      c: Double
    ): Int = {
      println(c)
      a
    }

    // TODO: check is overriding, using overriddenSymbols
    // not yet available from current scalameta version?
    def call2(
      a: Int, b: String // assert: Unused
    ): Int = 1
  }

}
