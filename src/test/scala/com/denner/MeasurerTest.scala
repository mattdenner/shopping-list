package com.denner

class MeasurerSuite extends org.scalatest.FunSuite {
  import Measurer._
  import Items._

  test("Full recipe") {
    Map(
      "1kg mixed dried fruit"             -> Item("mixed dried fruit",           None, Some(Measure(1000, None, MetricWeights.units))),
      "zest and juice 1 orange"           -> Item("orange, zest and juice",      None, Some(Measure(1,    None, NonUnits))),
      "zest and juice 1 lemon"            -> Item("lemon, zest and juice",       None, Some(Measure(1,    None, NonUnits))),
      "150ml brandy,Sherry,whisky or rum" -> Item("brandy,Sherry,whisky or rum", None, Some(Measure(150,  None, MetricVolumes.units))),
      "250g pack butter, softened"        -> Item("pack butter, softened",       None, Some(Measure(250,  None, MetricWeights.units))),
      "200g light soft brown sugar"       -> Item("light soft brown sugar",      None, Some(Measure(200,  None, MetricWeights.units))),
      "175g plain flour"                  -> Item("plain flour",                 None, Some(Measure(175,  None, MetricWeights.units))),
      "100g ground almonds"               -> Item("ground almonds",              None, Some(Measure(100,  None, MetricWeights.units))),
      "1/2 tsp baking powder"             -> Item("baking powder",               None, Some(Measure(0.5,  None, SpoonedMeasures.units))),
      "2 tsp mixed spice"                 -> Item("mixed spice",                 None, Some(Measure(2,    None, SpoonedMeasures.units))),
      "1 tsp ground cinnamon"             -> Item("ground cinnamon",             None, Some(Measure(1,    None, SpoonedMeasures.units))),
      "1/4 tsp ground cloves"             -> Item("ground cloves",               None, Some(Measure(0.25, None, SpoonedMeasures.units))),
      "100g flaked almonds"               -> Item("flaked almonds",              None, Some(Measure(100,  None, MetricWeights.units))),
      "4 large eggs"                      -> Item("eggs",                        None, Some(Measure(4,    None, UnscalableUnits("large")))),
      "1 tsp vanilla extract"             -> Item("vanilla extract",             None, Some(Measure(1,    None, SpoonedMeasures.units)))
    ).foreach {
      case (line, item) => assert(applySemantics(all)(Item(line)) === Some(item))
    }
  }
}
