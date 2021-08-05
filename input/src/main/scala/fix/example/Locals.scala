/*
rule = Unused
*/
package fix.example

package locals

object Test {
  val xs = {
    val x = 42
    List(x)
  }
}
