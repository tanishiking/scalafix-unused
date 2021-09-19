package fix

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._

import scalafix.testkit._
import org.scalatest.FunSuiteLike

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class BenchmarkSuite extends AbstractSemanticRuleSuite with FunSuiteLike {

  @Benchmark
  def run = {
    testsToRun.foreach { test =>
      val (rule, sdoc) = test.run.apply()
      rule.beforeStart()
      val res =
        try rule.semanticPatch(sdoc, suppress = false)
        finally rule.afterComplete()
    }
  }
}
