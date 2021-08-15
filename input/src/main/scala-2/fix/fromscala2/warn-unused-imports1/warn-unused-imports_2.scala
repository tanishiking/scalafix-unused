/*
rule = Unused
*/
package fix.fromscala2.`warn-unused-imports1`

import scala.collection.mutable._ // assert: Unused

// scalac: -Werror -Wunused:imports
//
class Bippo {
  def length: Int = 123
  class Tree
}

package object p1 {
  class A
  implicit class B(val s: String) { def bippy = s }
  val c: Bippo = new Bippo
  type D = String
}
package object p2 {
  class A
  implicit class B(val s: String) { def bippy = s }
  val c: Bippo = new Bippo
  type D = Int
}

object p3 {
  class A
  implicit class B(val s: String) { def bippy = s }
  val c: Bippo = new Bippo
  type D = Int
}

trait NoWarn {
  {
    import p1._ // no warn
    println("abc".bippy)
  }

  {
    import p1._ // no warn
    println(new A)
  }

  {
    import p1.B // no warn
    println("abc".bippy)
  }

  {
    import p1._ // no warn
    import c._
    println(length)
  }

  {
    import p1._ // no warn
    import c._
    val x: Tree = null
    println(x)
  }

  {
    import p1.D // no warn
    val x: D = null
    println(x)
  }
}

trait Warn {
  {
    import p1.A // assert: Unused
    println(123)
  }

  {
    import p1.{ A, B } // assert: Unused
    println("abc".bippy)
  }

  {
    import p1.{ A, B } // assert: Unused
    println(123)
  }

  {
    import p1._ // no warn (technically this could warn, but not worth the effort to unroll unusedness transitively)
    import c._
    println(123)
  }

  /*
  {
    // TODO: should be asserted
    import p1._
    println(123)
  }
  */

  {
    class Tree
    import p1._ // no warn
    import c._
    val x: Tree = null
    println(x)
  }

  {
    import p1.c._
    println(123)
  }

  {
    import p3._
    println("abc".bippy)
    import c._
    println(length)
  }
}

trait Nested {
  {
    import p1._
    trait Warn { // assert: Unused
      import p2._
      println(new A)
      println("abc".bippy)
    }
    println("")
  }

  {
    import p1._   // no warn
    trait NoWarn {
      import p2.B  // no warn
      println("abc".bippy)
      println(new A)
    }
    println(new NoWarn { })
  }

  {
    import p1.A // assert: Unused
    trait Warn {
      import p2.A
      println(new A)
    }
    println(new Warn { })
  }
}

// test unusage of imports from other compilation units after implicit search
trait Outsiders {
  {
    //implicit search should not disable warning
    import Sample._
    import Sample.Implicits._ // limitation, can't warn
    // f(42)                       // error
  }
  {
    import Sample._
    import Sample.Implicits._   // nowarn
    g(42)                       // ok
  }
  {
    import Sample._
    import Sample.Implicits.`int to Y`  // nowarn
    import Sample.Implicits.useless // assert: Unused
    g(42)                       // ok
  }
  {
    import java.io.File // assert: Unused
    import scala.concurrent.Future // assert: Unused
    import scala.concurrent.ExecutionContext.Implicits.global // assert: Unused
    import p1.A // assert: Unused
    import p1.B                         // no warn
    println("abc".bippy)
    //Future("abc".bippy)
  }
}

