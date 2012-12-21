package com.denner

class DirectMeasuresSuite extends org.scalatest.FunSuite {
  import Items._

  abstract class MeasurerDefinition {
    val measurer: Measurer.DirectMeasures
    val units: Seq[String]

    val unknownLines = Seq(
      "",
      "not foo",
      "1 packet foo",
      "1mg foo"
    ).map(Item(_))

    val amounts = Map[String,Double]("1/2" -> 0.5, "1" -> 1, "1 1/2" -> 1.5, "100" -> 100)

    def check() = {
      amounts.flatMap(
        (a) => units.map(
          (u) => Item(a._1 + u + " foo") -> Item("foo", None, Some(Measure(a._2, None, UnscalableUnits(u))))
        )
      ).foreach((e) => {
        assert(measurer.isDefinedAt(e._1))
        assert(measurer(e._1) === e._2)
      })
      unknownLines.foreach((l) => {
        assert(!measurer.isDefinedAt(l))
      })
    }
  }

  test("SizedMeasures") {
    new MeasurerDefinition {
      val units    = Seq("small", "medium", "large")
      val measurer = com.denner.Measurer.SizedMeasures

      check()
    }
  }

  test("ArbitraryMeasures") {
    new MeasurerDefinition {
      override val amounts = Map[String,Double]("a" -> 1, "1/2" -> 0.5, "1" -> 1, "1 1/2" -> 1.5, "100" -> 100)
      val units    = Seq("handful", "pinch", "dash", "bunch")
      val measurer = com.denner.Measurer.ArbitraryMeasures

      check()
    }
  }
}
