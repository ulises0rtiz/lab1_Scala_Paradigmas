import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import org.json4s._
import org.json4s.jackson.JsonMethods._
import Domain.Post

object TextProcessing {
    //funcion para formatear la fecha
    def formatDateFromUTC(utcSeconds: Long): String = {
        val instant = Instant.ofEpochSecond(utcSeconds)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))
        formatter.format(instant)
    }

    def parsePost(subredditName: String, jsonString:String): List[Post] ={
        implicit val formats: Formats = DefaultFormats
        val parsedJson = parse(jsonString)
        //el json de reddit es un arbol asique asi se llega a la lista de post
        val postList = (parsedJson \ "data" \ "children").children
        // transformar lista de nodos json en una List[Post]
        postList.map { postNode =>
            //entro al nodo data de cada post
            val data = postNode \ "data"
            // extraigo el title
            val title = (data \ "title").extract[String]
            // extraigo el selftext
            val selfText = (data \ "selftext").extract[String]
            // extraigo el created_utc
            val createdUtc = (data \ "created_utc").extract[Double].toLong
            // formateo la fecha
            val formattedDate = formatDateFromUTC(createdUtc)
            // retorno la tupla (subreddit, title, selftext, date)
            (subredditName, title, selfText, formattedDate)
            }
    }
}