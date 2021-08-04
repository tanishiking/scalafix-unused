/*
rule = Unused
Unused.params = false
Unused.locals = true
Unused.imports = false
Unused.privates = false
Unused.patvars = false
*/
package fix

def locals1() =
  val x = 1 /* assert: Unused
  ^^^^^^^^^
Unused definition 'x'
  */
  println(1)
