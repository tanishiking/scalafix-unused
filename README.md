# scalafix-unused
![CI](https://github.com/tanishiking/scalafix-unused/actions/workflows/ci.yml/badge.svg)

## Configuration
### Default Configuration
```
Unused {
  params = true,
  locals = ture,
  imports = true,
  privates = true,
  patvars = true,
}
```

## Limitations
### Wildcard import against non-package

```scala
import collection.mutable._ // ok (assert: Unused)

class Bippo {
  def length: Int = 123
  class Tree
}

object p1 {
  val c: Bippo = new Bippo
}

def foo = {
  {
    // scalafix-unused can't detect unused import
    import p1._
    println(123)
  }
  {
    // scalafix-unused can't detect unused import
    import p1.c._
    println(123)
  }
}

def bar = {
  import p1.c._
  println(length)
}
```

This limitation is required because `length` called from `def bar` has symbol `example::Bippo#length().`, and we have no information to tell the `length` is imported by `import pc.c._`.
