package com.denner

import org.scalatest.FunSuite
import com.denner.Items._

class ItemsSuite extends FunSuite {
  test("applySemantics") {
    val measurers = List(new PartialFunction[Item,Item] {
        def apply(item: Item) = Item("really")
        def isDefinedAt(item: Item) = item.line == "known"
    })

    assert(applySemantics(measurers)(Item("known"))   === Some(Item("really")))
    assert(applySemantics(measurers)(Item("unknown")) === Some(Item("unknown")))
  }

  test("+") {
    import scalaz._
    import Scalaz._

    def simpleItem(amount: Double, units: Units = NonUnits) =
      Item("foo", None, Some(Measure(amount, None, units)))

    assert(simpleItem(2) === (simpleItem(1) |+| simpleItem(1)))
    assert(simpleItem(2, UnscalableUnits("a")) == (simpleItem(1, UnscalableUnits("a")) |+| simpleItem(1, UnscalableUnits("a"))))
    intercept[MatchError] { simpleItem(1, UnscalableUnits("a")) |+| simpleItem(1, UnscalableUnits("b")) }
  }
}
