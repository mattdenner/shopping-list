package com.denner

import org.scalatest.FunSuite
import com.denner.Items._

class ItemsSuite extends FunSuite {
  trait Basic {
    case class Unknown(value: String)
  }

  test("applySemantics") {
    new Basic {
      assert(applySemantics(Seq())(Seq().toStream).isEmpty)
      assert(applySemantics(Seq())(Seq(Item("unknown")).toStream).toList === List(Item("unknown")))
    }

    new Basic {
      val measurers = List(new PartialFunction[Item,Item] {
          def apply(item: Item) = Item("really")
          def isDefinedAt(item: Item) = item.line == "known"
      })
      val expected = List("really", "unknown").map(Item(_))
      val list     = Seq("known", "unknown").map(Item(_))
      assert(applySemantics(measurers)(list.toStream).toList === expected)
    }
  }
}
