/*
rule = Unused
Unused.params = true
Unused.locals = false
Unused.imports = false
Unused.privates = false
Unused.patvars = false
*/
package fix

object Params {
  object Pos {
    def complete(isSuccess: Boolean): Unit = () /* assert: Unused
                 ^^^^^^^^^^^^^^^^^^
Parameter value 'isSuccess' in method 'complete' is never used
    */
    def f2(param: Boolean): Boolean = { /* assert: Unused
           ^^^^^^^^^^^^^^
Parameter value 'param' in method 'f2' is never used
      */
      val unused = true
      unused
    }
  }

  object Implicits {
    object Neg {
      def implicit1(x: Int)(implicit y: Int) = x + y

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

  object Neg {
    def neg0: Int = 1
    def neg1(x: Int): Int = x
    def neg2(x: Int, y: Int): Int = x + y
    def neg3(x: Int)(y: Int): Int = x + y
    def neg4(x: Int)(y: Int): Int = {
      println(x)
      y
    }
    def neg5(x: Int)(y: Int): Int = {
      val z = x
      z + y
    }
    def neg6(x: Option[Int], y: Option[Int]): Option[Int] = {
      for {
        x1 <- x
        y1 <- y
      } yield x1 + y1
    }
  }
  object HelloWorld {
    def main(args: Array[String]): Unit = { // no warn disabled
      println("Hello, world!")
    }
  }
}
