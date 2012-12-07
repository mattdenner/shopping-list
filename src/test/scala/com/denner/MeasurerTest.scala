package com.denner

class MeasurerSuite extends org.scalatest.FunSuite {
  import Measurer._
  import Items._

  test("Full recipe") {
    val recipe = """1kg mixed dried fruit
                   |zest and juice 1 orange
                   |zest and juice 1 lemon
                   |150ml brandy,Sherry,whisky or rum
                   |250g pack butter, softened
                   |200g light soft brown sugar
                   |175g plain flour
                   |100g ground almonds
                   |1/2 tsp baking powder
                   |2 tsp mixed spice
                   |1 tsp ground cinnamon
                   |1/4 tsp ground cloves
                   |100g flaked almonds
                   |4 large eggs
                   |1 tsp vanilla extract""".stripMargin

    val expected = List(
      Item("mixed dried fruit",           None, Some(Measure(1000, MetricWeights.units))),
      Item("orange, zest and juice",      None, Some(Measure(1,    NonUnits))),
      Item("lemon, zest and juice",       None, Some(Measure(1,    NonUnits))),
      Item("brandy,Sherry,whisky or rum", None, Some(Measure(150,  MetricVolumes.units))),
      Item("pack butter, softened",       None, Some(Measure(250,  MetricWeights.units))),
      Item("light soft brown sugar",      None, Some(Measure(200,  MetricWeights.units))),
      Item("plain flour",                 None, Some(Measure(175,  MetricWeights.units))),
      Item("ground almonds",              None, Some(Measure(100,  MetricWeights.units))),
      Item("baking powder",               None, Some(Measure(0.5,  SpoonedMeasures.units))),
      Item("mixed spice",                 None, Some(Measure(2,    SpoonedMeasures.units))),
      Item("ground cinnamon",             None, Some(Measure(1,    SpoonedMeasures.units))),
      Item("ground cloves",               None, Some(Measure(0.25, SpoonedMeasures.units))),
      Item("flaked almonds",              None, Some(Measure(100,  MetricWeights.units))),
      Item("eggs",                        None, Some(Measure(4,    UnscalableUnits("large")))),
      Item("vanilla extract",             None, Some(Measure(1,    SpoonedMeasures.units)))
    )

    assert(applySemantics(all)(recipe.split("\n").map(Item(_)).toStream).toList === expected)
  }
}
