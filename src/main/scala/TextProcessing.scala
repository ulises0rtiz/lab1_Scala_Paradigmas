import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import org.json4s._
import org.json4s.jackson.JsonMethods._
import Domain.Post
import scala.util.Try

object TextProcessing {

    val stopwords: Set[String] = Set(
        "the", "about", "above", "after", "again", "against", "all", "am", "an",
        "and", "any", "are", "aren't", "as", "at", "be", "because", "been",
        "before", "being", "below", "between", "both", "but", "by", "can't",
        "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't",
        "doing", "don't", "down", "during", "each", "few", "for", "from", "further",
        "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd",
        "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
        "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if",
        "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me",
        "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off",
        "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves",
        "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's",
        "should", "shouldn't", "so", "some", "such", "than", "that", "that's",
        "the", "their", "theirs", "them", "themselves", "then", "there", "there's",
        "these", "they", "they'd", "they'll", "re", "they've", "this", "those",
        "through", "to", "too", "under", "until", "up", "very", "was", "wasn't",
        "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what",
        "what's", "when", "when's", "where", "where's", "which", "while", "who",
        "who's", "whom", "why", "why's", "with", "won't", "would",
        "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
        "yourself", "yourselves"
    )
    //funcion para formatear la fecha
    def formatDateFromUTC(utcSeconds: Long): String = {
        val instant = Instant.ofEpochSecond(utcSeconds)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))
        formatter.format(instant)
    }

    def parsePost(subredditName: String, jsonString:String): List[Post] ={
        implicit val formats: Formats = DefaultFormats
        val parsedJsonOption = Try(parse(jsonString)).toOption
        //el json de reddit es un arbol asique asi se llega a la lista de post
        parsedJsonOption match {
            case Some(parsedJson) =>
                // Si el JSON es válido, intentamos llegar a la lista de posts
                val postList = Try((parsedJson \ "data" \ "children").children).getOrElse(List.empty)
                
                // Campos faltantes: Usamos flatMap en lugar de map. (para no doble lista)
                // flatMap automáticamente descarta los "None" de la lista final
                postList.flatMap { postNode =>
                    // encapsulamos la extracción en un Try, si ALGÚN campo falta o tiene mal tipo,
                    // Try falla silenciosamente y .toOption lo convierte en None
                    Try {
                        val data = postNode \ "data"
                        val title = (data \ "title").extract[String]
                        val selfText = (data \ "selftext").extract[String]
                        val createdUtc = (data \ "created_utc").extract[Double].toLong
                        val formattedDate = formatDateFromUTC(createdUtc)
                        
                        (subredditName, title, selfText, formattedDate)
                    }.toOption
                }
                
            case None => 
                // si el JSON estaba mal formado desde el principio, devolvemos lista vacía
                List.empty
        }
    }
    def computeWordFrequencies(post: List[Post]): Map[String, List[(String, Int)]] = {
        // agrupo los post por nombre del subreddit 
        val groupedBySubreddit = post.groupBy(post => post._1)
        // transformo los post de cada grupo en conteo de palabras
        groupedBySubreddit.map { case (subredditName, subredditPosts) =>
            // extraigo y separo palabras de titulos y cuerpos
            val allWords = subredditPosts.flatMap { case (_, title, selftext, _) =>
                val fullText = title + " " + selftext
                    //expresion regular que separa por cualquier caracter que no sea letra o numero
                    fullText.split("\\W+").filter(_.nonEmpty)
                }
            val filteredWords = allWords.filter { word =>
                val startsWithCapital = word.headOption.exists(_.isUpper)
                val isNotStopword = !stopwords.contains(word.toLowerCase)
                startsWithCapital && isNotStopword
            }    
            val frequencies = filteredWords.groupBy(identity).map { case (word, occurrences) =>
                (word, occurrences.size)
            }.toList
            val sortedFrequencies = frequencies.sortBy(pair => -pair._2)
            (subredditName, sortedFrequencies)
            }
    }
}