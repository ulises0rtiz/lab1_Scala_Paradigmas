import Domain.{Subscription, Post}

object Main {
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"
  // lee las suscripciones del archivo usando FileIO.readSubscriptions
    val subscriptions: List[Subscription] = FileIO.readSubscriptions("subscriptions.json")
  //desarmamos la tupla de cada suscripción usando map y descargamos los posts usando FileIO.downloadFeed
   val allPosts: List[Post] = subscriptions.flatMap { case (name, url) =>
      println(s"Fetching posts from: $name ($url)")
      val jsonStr = FileIO.downloadFeed(url)
      TextProcessing.parsePost(name, jsonStr)
    }
  // para cpmprobar imprimo la cantidad total de posts
    println(s"Total posts fetched: ${allPosts.length}")
    // imprimo post para ver como se ve
    allPosts.headOption.foreach(println)
  }
}
