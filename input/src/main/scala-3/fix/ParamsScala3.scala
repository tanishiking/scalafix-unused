/*
rule = Unused
Unused.params = true
Unused.locals = false
Unused.imports = false
Unused.privates = false
Unused.patvars = false
*/
package fix

object ParamsScala3:
  object Pos:
    def pos1(param: Boolean): Boolean = /* assert: Unused
             ^^^^^^^^^^^^^^
Unused parameter 'param'
    */
      val param = true
      param

    def pos2(x: Int)(using y: Int) = /* assert: Unused
                           ^^^^^^
Unused parameter 'y'
      */
      x
  end Pos

  object Neg:
    def neg1(x: Int)(using y: Int) =
      x + y
  end Neg
