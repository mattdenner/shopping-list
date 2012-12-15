package com.denner

import org.scalatest.FunSuite
import com.denner.Categorizer._

class CategorizerSuite extends FunSuite {
  import Items._

  trait MappingTest {
    val inputToExpected: Map[Item,Item]

    def execute() = inputToExpected.foreach {
      case (input, expected) => assert(applySemantics(all)(input) === Some(expected))
    }
  }

  test("Kept in order of JSON") {
    new MappingTest {
      val inputToExpected = Map(
        Item("beef stock")     -> Item("beef stock",     Some(Category("Cooking"))),
        Item("chicken stock")  -> Item("chicken stock",  Some(Category("Cooking"))),
        Item("beef steak")     -> Item("beef steak",     Some(Category("Meat"))),
        Item("chicken breast") -> Item("chicken breast", Some(Category("Meat")))
      )

      execute
    }
  }

  test("Full recipe") {
    new MappingTest {
      val inputToExpected = Map(
        Item("mixed dried fruit")           -> Item("mixed dried fruit",           Some(Category("Baking"))),
        Item("orange, zest and juice")      -> Item("orange, zest and juice",      Some(Category("Fruit"))),
        Item("lemon, zest and juice")       -> Item("lemon, zest and juice",       Some(Category("Fruit"))),
        Item("brandy,Sherry,whisky or rum") -> Item("brandy,Sherry,whisky or rum", Some(Category("Alcohol"))),
        Item("pack butter, softened")       -> Item("pack butter, softened",       Some(Category("Butter & spreads"))),
        Item("light soft brown sugar")      -> Item("light soft brown sugar",      Some(Category("Baking"))),
        Item("plain flour")                 -> Item("plain flour",                 Some(Category("Baking"))),
        Item("ground almonds")              -> Item("ground almonds",              Some(Category("Nuts"))),
        Item("baking powder")               -> Item("baking powder",               Some(Category("Baking"))),
        Item("mixed spice")                 -> Item("mixed spice",                 Some(Category("Herbs & spices"))),
        Item("ground cinnamon")             -> Item("ground cinnamon",             Some(Category("Herbs & spices"))),
        Item("ground cloves")               -> Item("ground cloves",               Some(Category("Herbs & spices"))),
        Item("flaked almonds")              -> Item("flaked almonds",              Some(Category("Nuts"))),
        Item("eggs")                        -> Item("eggs",                        Some(Category("Eggs"))),
        Item("vanilla extract")             -> Item("vanilla extract",             Some(Category("Baking")))
      )

      execute
    }
  }
}
