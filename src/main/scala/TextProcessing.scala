import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import org.json4s._
import org.json4s.jackson.JsonMethods._
import Domain.Post
import scala.util.Try

object TextProcessing {
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
}