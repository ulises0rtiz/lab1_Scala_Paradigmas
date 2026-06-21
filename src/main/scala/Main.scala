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
      val title = post._2
      val selftext = post._3
      // un post es válido si el título y el cuerpo no están vacíos o solo espacios
      val isTitleValid = title.trim.nonEmpty
      val isSelftextValid = selftext.trim.nonEmpty
      isTitleValid && isSelftextValid
    }
  println(s"\nTotal posts originales: ${allPosts.length}")
    println(s"Total posts válidos (filtrados): ${validPosts.length}")

    // Calculamos las frecuencias (ejercicio 5)
    val frequenciesPerSubreddit = TextProcessing.computeWordFrequencies(validPosts)
    
    // agrupamos los posts válidos por subreddit para el informe final
    val postsBySubreddit = validPosts.groupBy(post => post._1)

    // ejercicio 6 el informe final
    println(s"\n\n# INFORME FINAL DE REDDIT\n")

    postsBySubreddit.foreach { case (subreddit, posts) =>
      
      // FOLDLEFT: sumo los scores de forma inmutable para este subreddit
      val totalScore = posts.foldLeft(0) { (acumulador, post) =>
        acumulador + post._5 // El score está en la posición 5
      }

      println(s"## Subreddit: r/$subreddit")
      println(s"**Suma total de scores:** $totalScore\n")

      println("### Palabras más frecuentes:")
      val wordCounts = frequenciesPerSubreddit.getOrElse(subreddit, List.empty)
      wordCounts.take(5).foreach { case (word, count) =>
        println(s"- $word: $count")
      }

      println("\n### Top 5 Posts:")
      posts.take(5).foreach { post =>
        val title = post._2
        val date = post._4
        val url = post._6
        println(s"* [$date] $title")
        println(s"  Link: $url")
      }
      println("\n" + "-" * 50 + "\n")
    }
  }
}

