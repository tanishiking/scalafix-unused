/*
rule = Unused
*/
package fix.example

// see: https://github.com/scala/bug/issues/11387
class Foo
object Foo {
  private type Alias = Foo
  def x: Foo = new Alias
}
