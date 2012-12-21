package com.denner

import com.denner.Items._

/**
 * Maps an incoming string representing a line from the menu list, into something that is
 * possibly measured.
 */
abstract class Measurer extends PartialFunction[Item,Item] {

}

object Measurer {
  abstract class SimpleRegexMeasures(measureNames: Seq[String]) extends Measurer {
    lazy val Matcher = ("""^(\d+(?:\.\d+|/\d+|\s+\d/\d+)?)\s*(""" + measureNames.mkString("|") + """)(?:/[^\s]+)?\s+(.+)$""").r

    def apply(item: Item) = item.line match {
      case Matcher(amount, units, short) => Item(short, item.category, Some(measureFor(convertAmount(amount), units)))
    }

    def isDefinedAt(item: Item) = !Matcher.findFirstIn(item.line).isEmpty

    def measureFor(amount: Double, amountUnits: String): Measure

    val DecimalAmount = """(\d+(?:\.\d+)?)""".r
    val FractionalAmount = """((\d+)\s+)?(\d+)/(\d+)""".r

    def convertAmount(amountAsString: String) = amountAsString match {
      case DecimalAmount(value)                                => value.toDouble
      case FractionalAmount(_, null, numerator, denominator)   => numerator.toDouble / denominator.toDouble
      case FractionalAmount(_, amount, numerator, denominator) => amount.toDouble + (numerator.toDouble / denominator.toDouble)
    }
  }

  case class UnitMeasures(units: ScalableUnits) extends SimpleRegexMeasures(Seq(units.left, units.right)) {
    def measureFor(amount: Double, amountUnits: String) = Measure(units.adjustment(amountUnits)(amount), units)
  }

  case class DirectMeasures(units: Seq[UnscalableUnits]) extends SimpleRegexMeasures(units.map(_.name)) {
    def measureFor(amount: Double, amountUnits: String) = Measure(amount, units.find(_.name == amountUnits).get)
  }

  val MetricWeights   = new UnitMeasures(ScalableUnits("g",     "kg",    1000))
  val MetricVolumes   = new UnitMeasures(ScalableUnits("ml",    "l",     1000))
  val ImperialWeights = new UnitMeasures(ScalableUnits("oz",    "lb",    16))
  val ImperialVolumes = new UnitMeasures(ScalableUnits("pt",    "fl oz", 1.0 / 20)) // Pints more readable!
  val SpoonedMeasures = new UnitMeasures(ScalableUnits("tsp",   "tbsp",  3))

  val SizedMeasures     = new DirectMeasures(Seq("small", "medium", "large").map(UnscalableUnits(_)))
  val ArbitraryMeasures = new DirectMeasures(Seq("handful", "pinch", "dash").map(UnscalableUnits(_)))

  val CitrusMeasurer = new Measurer {
    private[this] val Matcher = """^(zest|juice|zest and juice|juice and zest)(?: of)? (\d+) (orange|lemon|lime)$""".r

    def apply(item: Item) = item.line match {
      case Matcher(stuff, amount, fruit) => Item(fruit + ", " + stuff, item.category, Some(Measure(amount.toDouble,NonUnits)))
    }

    def isDefinedAt(item: Item) = !Matcher.findFirstIn(item.line).isEmpty
  }

  val CountedMeasurer = new Measurer {
    private[this] val Matcher = """^(\d+) (.+)$""".r

    def apply(item: Item) = item.line match {
      case Matcher(amount, stuff) => Item(stuff, item.category, Some(Measure(amount.toDouble,NonUnits)))
    }

    def isDefinedAt(item: Item) = !Matcher.findFirstIn(item.line).isEmpty
  }

  val all = Seq(
    MetricWeights,   MetricVolumes,
    ImperialWeights, ImperialVolumes,
    SpoonedMeasures, SizedMeasures, ArbitraryMeasures,
    CitrusMeasurer,
    CountedMeasurer   // Really this is a last ditch attempt
  )
}
