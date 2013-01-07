package com.denner

class Main extends akka.actor.Actor with MainService {
  def actorRefFactory = context
  def receive         = runRoute(routeForRequests)
}

trait MainService extends spray.routing.HttpService {
  import com.denner.Items._
  import scalaz._
  import Scalaz._

  val routeForRequests = {
    import spray.http.HttpBody
    import spray.http.MediaTypes._
    import spray.httpx.unmarshalling.Unmarshaller
    import spray.httpx.marshalling.Marshaller

    import java.nio.charset.Charset

    import spray.json._
    import com.denner.Items.JsonProtocol._

    // Scope the unmarshaller for recipes posted in "text/plain" MIME type to this route
    implicit val RecipeUnmarshaller = Unmarshaller[List[String]](`text/plain`) {
      case HttpBody(_, buffer) => new String(buffer, Charset.forName("UTF-8")).split("\n").toList.filterNot((x) => x.isEmpty || x.startsWith("#"))
    }

    // Scope the marshaller for parsed data in "application/json" MIME type to this route
    implicit val ShoppingListMarshaller = Marshaller.delegate[Seq[Item], String](`application/json`)(_.toJson.toString)

    // We take in a text body that is essentially a list of recipes, and turn that into a
    // map from category to the items that need purchasing, serialized as JSON.
    path("") {
      get {
        getFromResource("index.html")
      } ~
      post {
        entity(as[List[String]]) { parsedRecipe =>
          produce(instanceOf[Seq[Item]]) {
            serialize => _ => serialize(categorizeRecipe(parsedRecipe))
          }
        }
      }
    } ~ /* What follows is the handling of resources */
    path("js"        / Rest) { path => get { getFromResource("js/" + path) } } ~
    path("css"       / Rest) { path => get { getFromResource("css/" + path) } } ~
    path("img"       / Rest) { path => get { getFromResource("image/" + path) } } ~
    path("templates" / Rest) { path => get { getFromResource("templates/" + path) } }
  }

  private[this] lazy val measure    = applySemantics(Measurer.all)(_)
  private[this] lazy val categorize = applySemantics(Categorizer.all)(_)

  private[this] def categorizeRecipe(lines: Seq[String]): Seq[Item] = {
    val categorized = for {
      item        <- lines.map(Item(_));
      measured    <- measure(item);
      categorized <- categorize(measured)
    } yield categorized

    categorized.groupBy(_.line.toLowerCase).values.toSeq.map(_.reduce(_ |+| _))
  }
}
