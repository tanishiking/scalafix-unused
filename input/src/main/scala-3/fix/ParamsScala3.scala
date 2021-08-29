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
Parameter value 'param' in method 'pos1' is never used
    */
      val param = true
      param

    def pos2(x: Int)(using y: Int) = /* assert: Unused
                           ^^^^^^
Parameter value 'y' in method 'pos2' is never used
      */
      x
  end Pos

  object Neg:
    def neg1(x: Int)(using y: Int) =
      x + y
  end Neg
