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
      Item("beef stock",     Some(Category("Cooking"))),
      Item("chicken stock",  Some(Category("Cooking"))),
      Item("beef steak",     Some(Category("Meat"))),
      Item("chicken breast", Some(Category("Meat")))
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
      Item("mixed dried fruit",           Some(Category("Baking"))),
      Item("orange, zest and juice",      Some(Category("Fruit"))),
      Item("lemon, zest and juice",       Some(Category("Fruit"))),
      Item("brandy,Sherry,whisky or rum", Some(Category("Alcohol"))),
      Item("pack butter, softened",       Some(Category("Butter & spreads"))),
      Item("light soft brown sugar",      Some(Category("Baking"))),
      Item("plain flour",                 Some(Category("Baking"))),
      Item("ground almonds",              Some(Category("Nuts"))),
      Item("baking powder",               Some(Category("Baking"))),
      Item("mixed spice",                 Some(Category("Herbs & spices"))),
      Item("ground cinnamon",             Some(Category("Herbs & spices"))),
      Item("ground cloves",               Some(Category("Herbs & spices"))),
      Item("flaked almonds",              Some(Category("Nuts"))),
      Item("eggs",                        Some(Category("Eggs"))),
      Item("vanilla extract",             Some(Category("Baking")))
    )

    assert(applySemantics(all)(input.toStream).toList === expected)
  }
}
