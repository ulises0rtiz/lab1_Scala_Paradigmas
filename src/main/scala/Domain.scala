object Domain {
  type Subscription = (String, String)
  type Post = (String, String, String, String) // (subreddit, title, selftext, date)
}