/*
rule = Unused
Unused.params = false
Unused.locals = false
Unused.imports = false
Unused.privates = true
Unused.patvars = false
*/

package fix

// Scala3 requires https://github.com/lampepfl/dotty/pull/12964 to be released
object PrivatesPositive {
  object PrivatesObj {
    private def foo(x: Int) = ??? /* assert: Unused
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Private definition 'foo' in 'PrivatesObj' is never used
    */

    private object Foo { /* assert: Unused
    ^
Private definition 'Foo' in 'PrivatesObj' is never used
      */
      def x = 1
    }

    private type A = Int /* assert: Unused
    ^^^^^^^^^^^^^^^^^^^^
Private definition 'A' in 'PrivatesObj' is never used
    */

    private val field = 1 /* assert: Unused
    ^^^^^^^^^^^^^^^^^^^^^
Private definition 'field' in 'PrivatesObj' is never used
    */
  }

  trait PrivatesTrait {
    private def foo(x: Int) = ??? // assert: Unused
    private object Foo { // assert: Unused
      def x = 1
    }
    private type A = Int // assert: Unused
    private val field = 1 // assert: Unused
  }

  case class PrivateClass() { // should be asserted
    private def foo(x: Int) = ??? // assert: Unused
    private object Foo { // assert: Unused
      def x = 1
    }
    private type A = Int // assert: Unused
    private val field = 1 // assert: Unused
  }

  case class PrivateClass2(
      private val x: Int,
      private val y: Int // assert: Unused
  ) {
    def foo = x
  }

}


object PrivateNegative {
  case class Foo(x: Int, y: Int)

  object PrivatesObj {
    class Foo(private val x: Int) {
      def fooProxy = foo(x)
    }

    private def foo(x: Int) = ???

    private object Foo {
      val x = 1
    }
    val foox = Foo.x

    val a: A = 1
    private type A = Int

    def getField = {
      println(field)
    }
    private var field = 1
  }
}
