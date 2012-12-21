package com.denner

import com.denner.Items._

/**
 * Maps an incoming string representing a line from the menu list, into something that is
 * possibly measured.
 */
abstract class Measurer extends PartialFunction[Item,Item] {

}

object Measurer {
  abstract class SimpleRegexMeasures(prefixGroup: String, measureNames: Seq[String]) extends Measurer {
    lazy val Matcher = ("""^""" + prefixGroup + """\s*(""" + measureNames.mkString("|") + """)(?:/[^\s]+)?\s+(.+)$""").r

    def apply(item: Item) = item.line match {
      case Matcher(minimum, units, short) => Item(
        short,
        item.category,
        Some(measureFor(convertAmount(minimum).get, None, units))
      )
      case Matcher(minimum, maximum, units, short) => Item(
        short,
        item.category,
        Some(measureFor(convertAmount(minimum).get, convertAmount(maximum), units))
      )
    }

    def isDefinedAt(item: Item) = !Matcher.findFirstIn(item.line).isEmpty

    def measureFor(minimum: Double, maximum: Option[Double], amountUnits: String): Measure

    val DecimalAmount    = """^(\d+(?:\.\d+)?)$""".r
    val FractionalAmount = """^((\d+)\s+)?(\d+)/(\d+)$""".r

    def convertAmount(amountAsString: String): Option[Double] = amountAsString match {
      case DecimalAmount(value)                                => Some(value.toDouble)
      case FractionalAmount(_, null, numerator, denominator)   => Some(numerator.toDouble / denominator.toDouble)
      case FractionalAmount(_, amount, numerator, denominator) => Some(amount.toDouble + (numerator.toDouble / denominator.toDouble))
      case null                                                => None
    }
  }

  // Regex for dealing with numeric values in strings
  private[this] val NumericValue  = """\d+(?:\.\d+|/\d+|\s+\d/\d+)?"""

  case class UnitMeasures(units: ScalableUnits) extends SimpleRegexMeasures("("+NumericValue+")(?:-("+NumericValue+"))?", Seq(units.left, units.right)) {
    def measureFor(minimum: Double, maximum: Option[Double], amountUnits: String) =
      Measure(units.adjustment(amountUnits)(minimum), maximum, units)
  }

  case class DirectMeasures(units: Seq[UnscalableUnits]) extends SimpleRegexMeasures("(a|"+NumericValue+")", units.map(_.name)) {
    def measureFor(minimum: Double, maximum: Option[Double], amountUnits: String) =
      Measure(minimum, maximum, units.find(_.name == amountUnits).get)

    override def convertAmount(amountAsString: String): Option[Double] = amountAsString match {
      case "a" => Some(1)
      case _   => super.convertAmount(amountAsString)
    }
  }

  val MetricWeights   = new UnitMeasures(ScalableUnits("g",     "kg",    1000))
  val MetricVolumes   = new UnitMeasures(ScalableUnits("ml",    "l",     1000))
  val ImperialWeights = new UnitMeasures(ScalableUnits("oz",    "lb",    16))
  val ImperialVolumes = new UnitMeasures(ScalableUnits("pt",    "fl oz", 1.0 / 20)) // Pints more readable!
  val SpoonedMeasures = new UnitMeasures(ScalableUnits("tsp",   "tbsp",  3))

  val SizedMeasures     = new DirectMeasures(Seq("small", "medium", "large").map(UnscalableUnits(_)))
  val ArbitraryMeasures = new DirectMeasures(Seq("handful", "pinch", "dash", "bunch").map(UnscalableUnits(_)))

  val CitrusMeasurer = new Measurer {
    private[this] val Matcher = """^(zest|juice|zest and juice|juice and zest)(?: of)? (\d+) (orange|lemon|lime)$""".r

    def apply(item: Item) = item.line match {
      case Matcher(stuff, amount, fruit) => Item(fruit + ", " + stuff, item.category, Some(Measure(amount.toDouble,None,NonUnits)))
    }

    def isDefinedAt(item: Item) = !Matcher.findFirstIn(item.line).isEmpty
  }

  val CountedMeasurer = new Measurer {
    private[this] val Matcher = """^(\d+) (.+)$""".r

    def apply(item: Item) = item.line match {
      case Matcher(amount, stuff) => Item(stuff, item.category, Some(Measure(amount.toDouble,None,NonUnits)))
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
