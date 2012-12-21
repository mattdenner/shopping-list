package com.denner

class NonMeasuredTest extends org.scalatest.FunSuite {
  import Measurer._
  import Items._

  test("CitrusMeasures") {
    val types = Seq("zest", "juice", "zest and juice", "juice and zest")
    val fruit = Seq("orange", "lemon", "lime")

    types.flatMap(
      (t) => fruit.map(
        (f) => Item(t + " of 1 " + f) -> Item(f + ", " + t, None, Some(Measure(1,None,NonUnits)))
      ) ++ fruit.map(
        (f) => Item(t + " 1 " + f) -> Item(f + ", " + t, None, Some(Measure(1,None,NonUnits)))
      )
    ).foreach((e) => {
      assert(CitrusMeasurer.isDefinedAt(e._1))
      assert(CitrusMeasurer(e._1) === e._2)
    })
  }

  test("CountedMeasures") {
    assert(CountedMeasurer.isDefinedAt(Item("1 football")))
    assert(CountedMeasurer(Item("1 football")) === Item("football", None, Some(Measure(1,None,NonUnits))))
  }
}
