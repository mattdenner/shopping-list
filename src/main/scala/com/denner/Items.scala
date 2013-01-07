package com.denner

object Items {
  import scalaz._
  import Scalaz._

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

  implicit object MeasureSemigroup extends Semigroup[Measure] {
    def append(left: Measure, right: => Measure) = {
      def internalAdd(measure: Measure) = (left.minimum+right.minimum, add(left.maximum, right.maximum)) match {
        case (minimum, Some(maximum)) if minimum < maximum => Measure(minimum, Some(maximum), left.units)
        case (minimum, Some(maximum)) if minimum > maximum => Measure(maximum, Some(minimum), left.units)
        case (minimum, maximum)                            => Measure(minimum, maximum, left.units)
      }

      def add(left: Option[Double], right: Option[Double]) = (left,right) match {
        case (Some(a), Some(b)) => Some(a+b)
        case (Some(a), None)    => Some(a)
        case (None,    Some(b)) => Some(b)
        case _                  => None
      }

      right match {
        case Measure(_, _, units) if units == left.units => internalAdd(right)
      }
    }
  }

  type Measured = Option[Measure]

  // Things can be categorised, or not
  case class Category(name: String)
  type Categorized = Option[Category]

  // An item is a line in the recipe list, which may have been placed in a category and
  // measured for an amount.
  case class Item(line: String, category: Categorized = None, amount: Measured = None)

  // Item can be considered a semigroup, which means that two Item instances can be appended
  // together to give another Item, essentially addition.  There are rules that govern this,
  // ensuring that two items that are ill-measured cannot be added together.
  implicit object ItemSemigroup extends Semigroup[Item] {
    def append(left: Item, right: => Item) = (left, right) match {
      case (Item(a, Some(ac), Some(am)), Item(b, Some(bc), Some(bm))) if ac == bc => Item(a, Some(ac), Some(am |+| bm))
      case (Item(a,     None, Some(am)), Item(b,     None, Some(bm)))             => Item(a,     None, Some(am |+| bm))
    }
  }

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
    implicit val ItemFormat     = jsonFormat3(Item.apply)
  }

  /**
   * Passes each of the input values from the sequence through the sequence of partial functions,
   * returning either the output value from the first fitting function, or the unknown value.
   */
  def applySemantics(applicators: Seq[PartialFunction[Item,Item]])(item: Item): Option[Item] =
    applicators.collectFirst { case a if a.isDefinedAt(item) => a(item) }.orElse(Some(item))
}
