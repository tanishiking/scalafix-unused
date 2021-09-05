# scalafix-unused

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ec81c689ca784994bdda93a8134be2a9)](https://app.codacy.com/gh/tanishiking/scalafix-unused?utm_source=github.com&utm_medium=referral&utm_content=tanishiking/scalafix-unused&utm_campaign=Badge_Grade_Settings)

![CI](https://github.com/tanishiking/scalafix-unused/actions/workflows/ci.yml/badge.svg)

## Configuration
### Default Configuration
```scala
Unused {
  params = true,
  locals = true,
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
