/*
rule = Unused
*/
package fix.example

class Local {
  def a() = {
    def id[A](a: A): A = a
    id(1)
  }
}
