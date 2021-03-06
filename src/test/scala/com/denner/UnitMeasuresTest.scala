package com.denner

class UnitMeasuresTest extends org.scalatest.FunSuite {
  import Items._
  import scalaz._
  import Scalaz._

  abstract class MeasurerDefinition {
    val measurer: Measurer.UnitMeasures
    val units: Seq[String]

    val unknownLines = Seq(
      "",
      "not foo",
      "1 packet foo",
      "1mg foo"
    ).map(Item(_))

    val amounts = Map[String,(Double,Option[Double])](
      "1/2"     -> (0.5, None),
      "1"       -> (1,   None),
      "1 1/2"   -> (1.5, None),
      "100"     -> (100, None),
      "1-2"     -> (1,   Some(2)),
      "1 1/2-2" -> (1.5, Some(2))
    )

    def check() = {
      amounts.flatMap(
        (a) => units.map(
          (u) => Item(a._1 + u + " foo") -> Item(
            "foo",
            None,
            Some(Measure(
              measurer.units.adjustment(u)(a._2._1),
              a._2._2.map(measurer.units.adjustment(u)),
              measurer.units)
            )
          )
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

  test("addition") {
    assert(Measure(0, None, NonUnits) === (Measure(0, None, NonUnits) |+| Measure(0, None, NonUnits)))
    assert(Measure(1, None, NonUnits) === (Measure(1, None, NonUnits) |+| Measure(0, None, NonUnits)))
    assert(Measure(1, None, NonUnits) === (Measure(0, None, NonUnits) |+| Measure(1, None, NonUnits)))
    assert(Measure(2, None, NonUnits) === (Measure(1, None, NonUnits) |+| Measure(1, None, NonUnits)))

    assert(Measure(0, Some(1), NonUnits) === (Measure(0, Some(1), NonUnits) |+| Measure(0, None,    NonUnits)))
    assert(Measure(0, Some(1), NonUnits) === (Measure(0, None,    NonUnits) |+| Measure(0, Some(1), NonUnits)))
    assert(Measure(0, Some(2), NonUnits) === (Measure(0, Some(1), NonUnits) |+| Measure(0, Some(1), NonUnits)))
    assert(Measure(2, Some(2), NonUnits) === (Measure(1, Some(1), NonUnits) |+| Measure(1, Some(1), NonUnits)))
    assert(Measure(2, Some(3), NonUnits) === (Measure(1, Some(1), NonUnits) |+| Measure(2, Some(1), NonUnits)))

    assert(Measure(2, None, UnscalableUnits("test")) === (Measure(1, None, UnscalableUnits("test")) |+| Measure(1, None, UnscalableUnits("test"))))
    intercept[MatchError] { Measure(1, None, NonUnits) |+| Measure(1, None, UnscalableUnits("test")) }

    assert(Measure(2, None, ScalableUnits("g", "kg", 1000)) === (Measure(1, None, ScalableUnits("g", "kg", 1000)) |+| Measure(1, None, ScalableUnits("g", "kg", 1000))))
    intercept[MatchError] { Measure(1, None, ScalableUnits("g", "kg", 1000)) |+| Measure(1, None, ScalableUnits("oz", "lb", 1000)) }
  }
}
