object Domain {
  type Subscription = (String, String)
  // Agregamos score (Int) y permalink (String)
  // (subreddit, title, selftext, date, score, permalink)
  type Post = (String, String, String, String, Int, String) 
}