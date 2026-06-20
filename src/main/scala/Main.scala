import Domain.Subscription

object Main {
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"
  // lee las suscripciones del archivo usando FileIO.readSubscriptions
    val subscriptions: List[Subscription] = FileIO.readSubscriptions("subscriptions.json")
  //desarmamos la tupla de cada suscripción usando map y descargamos los posts usando FileIO.downloadFeed
   val allPosts: List[(String, String)] = subscriptions.map { case (name, url) =>
      println(s"Fetching posts from: $name ($url)")
      val posts = FileIO.downloadFeed(url)
      (name, posts)
    }

    val output = allPosts
      .map { case (name, posts) => Formatters.formatSubscription(name, posts) }
      .mkString("\n")

    println(output)
  }
}
