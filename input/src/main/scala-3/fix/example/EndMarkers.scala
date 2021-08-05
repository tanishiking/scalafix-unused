/*
rule = Unused
*/

package fix.example

// should be able to write `package endmarkers:` scalameta parser issue
// package endmarkers:
package endmarkers

class MultiCtor(val i: Int):
  def this() =
    this(23)
  end this
end MultiCtor

def topLevelMethod: String =
  "hello"
end topLevelMethod

val topLevelVal: Int =
  23
end topLevelVal

var topLevelVar: String =
  ""
end topLevelVar

class Container:

  def foo =
    (1,2,3)
  end foo

  val bar =
    (4,5,6)
  end bar

  var baz =
    15
  end baz

end Container

def topLevelWithLocals: Unit =

  val localVal = // assert: Unused
    37
  end localVal

  var localVar = // assert: Unused
    43
  end localVar

  def localDef =
    97
  end localDef

end topLevelWithLocals

object TestObj:

  def foo = 23

end TestObj
