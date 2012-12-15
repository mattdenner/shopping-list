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
}
