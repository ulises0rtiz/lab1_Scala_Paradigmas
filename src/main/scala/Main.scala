import Domain.{Subscription, Post}

object Main {
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"
  // lee las suscripciones del archivo usando FileIO.readSubscriptions
    val subscriptions: List[Subscription] = FileIO.readSubscriptions("subscriptions.json")
  //desarmamos la tupla de cada suscripción usando map y descargamos los posts usando FileIO.downloadFeed
   val allPosts: List[Post] = subscriptions.flatMap { case (name, url) =>
      println(s"Fetching posts from: $name ($url)")
      val jsonStringOption = FileIO.downloadFeed(url)
      jsonStringOption match {
        case Some(jsonString) =>
          // parseamos el jsonString a una lista de posts usando TextProcessing.parsePost
          TextProcessing.parsePost(name, jsonString)
        case None =>
          println(s"Failed to download feed from: $name ($url)")
          List.empty[Post]
      }
    }
  // ahora tenemos una lista de posts, pero algunos pueden tener campos vacíos o solo espacios

    val validPosts: List[Post] = allPosts.filter { post =>
    // desarmamos la tupla para acceder a cada campo facil
    val (subreddit, title, selftext, date) = post
    // valido que el titulo y el texto tengan contenido real (no solo espacios)
    val isTitleValid = title.trim.nonEmpty
    val isSelfTextValid = selftext.trim.nonEmpty
    // el post es valido si ambos campos son validos
    isTitleValid && isSelfTextValid
    }
  // para cpmprobar imprimo la cantidad total de posts antes y despues del filtro
    println(s"\nTotal posts originales: ${allPosts.length}")
    println(s"Total posts válidos (filtrados): ${validPosts.length}")
    // imprimo post para ver como se ve
    println("\nPrimer post válido:")
    // En lugar de println(post), usa tu nuevo formateador:
    validPosts.headOption.foreach(post => println(Formatters.formatPost(post)))
  }
}
