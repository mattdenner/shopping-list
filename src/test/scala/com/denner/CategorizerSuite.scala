package com.denner

import org.scalatest.FunSuite
import com.denner.Categorizer._

class CategorizerSuite extends FunSuite {
  import Items._

  test("Kept in order of JSON") {
    val input = List(
      Item("beef stock"),
      Item("chicken stock"),
      Item("beef steak"),
      Item("chicken breast")
    )

    val expected = List(
      Item("beef stock",     Some(Category("cooking"))),
      Item("chicken stock",  Some(Category("cooking"))),
      Item("beef steak",     Some(Category("meat"))),
      Item("chicken breast", Some(Category("meat")))
    )

    assert(applySemantics(all)(input.toStream).toList === expected)
  }

  test("Full recipe") {
    val input = List(
      Item("mixed dried fruit"),
      Item("orange, zest and juice"),
      Item("lemon, zest and juice"),
      Item("brandy,Sherry,whisky or rum"),
      Item("pack butter, softened"),
      Item("light soft brown sugar"),
      Item("plain flour"),
      Item("ground almonds"),
      Item("baking powder"),
      Item("mixed spice"),
      Item("ground cinnamon"),
      Item("ground cloves"),
      Item("flaked almonds"),
      Item("eggs"),
      Item("vanilla extract")
    )

    val expected = List(
      Item("mixed dried fruit",           Some(Category("baking"))),
      Item("orange, zest and juice",      Some(Category("fruit"))),
      Item("lemon, zest and juice",       Some(Category("fruit"))),
      Item("brandy,Sherry,whisky or rum", Some(Category("alcohol"))),
      Item("pack butter, softened",       Some(Category("dairy"))),
      Item("light soft brown sugar",      Some(Category("baking"))),
      Item("plain flour",                 Some(Category("baking"))),
      Item("ground almonds",              Some(Category("nuts"))),
      Item("baking powder",               Some(Category("baking"))),
      Item("mixed spice",                 Some(Category("herbs & spices"))),
      Item("ground cinnamon",             Some(Category("herbs & spices"))),
      Item("ground cloves",               Some(Category("herbs & spices"))),
      Item("flaked almonds",              Some(Category("nuts"))),
      Item("eggs",                        Some(Category("dairy"))),
      Item("vanilla extract",             Some(Category("baking")))
    )

    assert(applySemantics(all)(input.toStream).toList === expected)
  }
}
