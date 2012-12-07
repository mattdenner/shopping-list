package com.denner

class UnitMeasuresTest extends org.scalatest.FunSuite {
  import Items._

  abstract class MeasurerDefinition {
    val measurer: Measurer.UnitMeasures
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
          (u) => Item(a._1 + u + " foo") -> Item("foo", None, Some(Measure(measurer.units.adjustment(u)(a._2), measurer.units)))
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

  test("MetricWeights") {
    new MeasurerDefinition {
      val units    = Seq("g", "kg")
      val measurer = com.denner.Measurer.MetricWeights
      def unitLookup(name: String) = measurer.units

      check()
    }
  }

  test("MetricVolumes") {
    new MeasurerDefinition {
      val units    = Seq("ml", "l")
      val measurer = com.denner.Measurer.MetricVolumes
      def unitLookup(name: String) = measurer.units

      check()
    }
  }

  test("ImperialWeights") {
    new MeasurerDefinition {
      val units    = Seq("lb", "oz")
      val measurer = com.denner.Measurer.ImperialWeights
      def unitLookup(name: String) = measurer.units

      check()
    }
  }

  test("ImperialVolumes") {
    new MeasurerDefinition {
      val units    = Seq("fl oz", "pt")
      val measurer = com.denner.Measurer.ImperialVolumes
      def unitLookup(name: String) = measurer.units

      check()
    }
  }

  test("SpoonedMeasures") {
    new MeasurerDefinition {
      val units    = Seq("tsp", "tbsp")
      val measurer = com.denner.Measurer.SpoonedMeasures

      check()
    }
  }
}
