/*
rule = Unused
Unused.params = false
Unused.locals = true
Unused.imports = false
Unused.privates = false
Unused.patvars = false
*/
package fix

object Locals {
  object Pos {
    def complete(isSuccess: Boolean): Unit = {
      val x = 1 /* assert: Unused
      ^^^^^^^^^
Unused definition 'x'
      */
    }

    def variable: Unit = {
      var x = 1 /* assert: Unused
      ^^^^^^^^^
Unused definition 'x'
      */
    }

    def implicit1(): Unit = {
      implicit val x = 1 /* assert: Unused
      ^^^^^^^^^^^^^^^^^^
Unused definition 'x'
      */
    }
  }

  object Neg {
    def complete(isSuccess: Boolean): Unit = {
      val x = 1
      println(x)
    }

    def wildcard: Unit = {
      val _ = 1
    }


    def patvars = {
      val (a, _) = (1, 2) // this will be catched by patvars
    }
  }
}
