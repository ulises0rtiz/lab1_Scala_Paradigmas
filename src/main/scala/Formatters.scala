
object Formatters {

  // Pure function to format posts from a subscription
  def formatSubscription(url: String, posts: String): String = {
    val header = s"\n${"=" * 80}\nPosts from: $url \n${"=" * 80}"
    val formattedPosts = posts.take(80)
    header + "\n" + formattedPosts
  } 
  def formatPost(post: Domain.Post): String = {
    val (subreddit, title, selftext, date) = post
    s"""
    |================================================================================
    | SUBREDDIT: $subreddit
    | FECHA:     $date
    | TÍTULO:    $title
    |--------------------------------------------------------------------------------
    | TEXTO:     $selftext
    |================================================================================
    """.stripMargin
  }
}
