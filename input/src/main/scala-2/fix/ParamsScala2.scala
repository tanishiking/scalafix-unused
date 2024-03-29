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
    // TODO: Scala3
    // Parameter value 'x' in method 'implicit2' is never used
    def implicit2(implicit x: Int) = implicit1(1)
  }
  object Overridden {
    trait Interface {
      def call1(x: Int): Unit
      def call2(x: Int): Unit
    }

    class Concrete extends Interface {
      override def call1(x: Int): Unit = ()

      // TODO: shuldn't assert for override method
      // see: https://github.com/scalacenter/scalafix/issues/1476
      def call2(x: Int): Unit = () // assert: Unused
    }

  }


}
