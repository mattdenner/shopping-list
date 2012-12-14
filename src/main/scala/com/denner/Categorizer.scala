package com.denner

import com.denner.Items._

/**
 * Maps a possibly measured item into a possibly categorised one.
 */
abstract class Categorizer extends PartialFunction[Item,Item] {

}

object Categorizer {
  class WordBoundedCategorizer(items: List[String], name: String) extends Categorizer {
    private[this] val category              = Category(name)
    private[this] val Matcher               = ("""\b(""" + items.mkString("(?:es|s)?|") + """(?:es|s)?)\b""").r

    def apply(item: Item)       = Item(item.line, Some(category), item.amount)
    def isDefinedAt(item: Item) = !Matcher.findFirstIn(item.line.toLowerCase).isEmpty
  }

  type CategoryEntriesPair = (String,List[String])
  type Categories          = List[CategoryEntriesPair]

  /**
   * Loads the category definitions from the specified resource file, which is a
   * JSON map from category name to a list of things that are in that category.  It
   * maintains the order of the file, which can be useful for dealing with clashes (like
   * "beef" and "beef stock").
   */
  private[this] def loadJson(resourceName: String): Categories = {
    import net.liftweb.json._

    def decodeJson(json: JValue) = {
      def parseCategories(categories: List[JField]) = categories.map {
        case JField(name,JArray(values)) => (name,values.map { case JString(value) => value })
      }
      def decodeJsonToCategories(list: Categories, field: JValue) = field match {
        case JObject(categories) => parseCategories(categories) ++ list
        case _                   => list
      }
      json.fold(List[CategoryEntriesPair]())(decodeJsonToCategories(_, _))
    }

    decodeJson(parse(io.Source.fromInputStream(getClass.getResource(resourceName).openStream).mkString))
  }

  lazy val all: Seq[Categorizer] = loadJson("/com/denner/categories.json").map {
    case (category, things) => new WordBoundedCategorizer(things, category)
  }.toSeq
}
