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
}
