package com.denner

object Items {
  // Measured things have units, or the lack of units!
  trait Units {
    protected val Identity = (x: Double) => x
    def adjustment(units: String): (Double) => Double
  }
  object NonUnits extends Units {
    def adjustment(units: String) = Identity
    override def toString = ""
  }
  case class UnscalableUnits(name: String) extends Units {
    def adjustment(units: String) = Identity
    override def toString = name
  }
  case class ScalableUnits(left: String, right: String, scale: Double) extends Units {
    def converter = (x: Double) => x * scale
    def adjustment(units: String) = if (units == left) Identity else converter
    override def toString = left
  }

  // Things need quantity, but those amounts have units so that you can't miss compare.
  case class Measure(minimum: Double, maximum: Option[Double], units: Units) {
    override def toString = minimum + maximumString + units.toString

    private[this] def maximumString = maximum match {
      case Some(value) => "-" + value
      case None        => ""
    }
  }
  type Measured = Option[Measure]

  // Things can be categorised, or not
  case class Category(name: String)
  type Categorized = Option[Category]

  // An item is a line in the recipe list, which may have been placed in a category and
  // measured for an amount.
  case class Item(line: String, category: Categorized = None, amount: Measured = None)

  // JSON serialization
  object JsonProtocol extends spray.json.DefaultJsonProtocol {
    import spray.json.{RootJsonFormat, JsString, JsValue, deserializationError}

    implicit val UnitsFormat = new RootJsonFormat[Units] {
      def write(units: Units)  = JsString(units.toString)
      def read(value: JsValue) = deserializationError("Unsupported")
    }

    implicit val CategoryFormat = new RootJsonFormat[Category] {
      def write(category: Category) = JsString(category.name)
      def read(value: JsValue) = value match { case JsString(name) => Category(name) }
    }

    implicit val MeasureFormat  = jsonFormat3(Measure)
    implicit val ItemFormat     = jsonFormat3(Item)
  }

  /**
   * Passes each of the input values from the sequence through the sequence of partial functions,
   * returning either the output value from the first fitting function, or the unknown value.
   */
  def applySemantics(applicators: Seq[PartialFunction[Item,Item]])(item: Item): Option[Item] =
    applicators.collectFirst { case a if a.isDefinedAt(item) => a(item) }.orElse(Some(item))
}
