import scala.io.Source
import Domain.Subscription
import scala.io.Source
import scala.util.Using
import org.json4s._
import org.json4s.jackson.JsonMethods._

object FileIO {
  // Pure function to read subscriptions from a JSON file
  def readSubscriptions(path: String): List[Subscription] = {
    // Requisito de json4s
    implicit val formats: Formats = DefaultFormats

    // Using lee el archivo y lo cierra automáticamente al terminar
    val tryResult = Using(Source.fromFile(path)) { source =>
      val jsonText = source.mkString
      
      // Convertimos el texto a una lista de diccionarios
      val jsonList = parse(jsonText).extract[List[Map[String, String]]]

      // REQUISITO: Crear cada Subscription usando map
      jsonList.map { dict =>
        // 1. Extrae el nombre usando: dict.getOrElse("name", "")
        // 2. Extrae la url usando: dict.getOrElse("url", "")
        // 3. Devuelve la tupla: (nombre, url)
        val nombre = dict.getOrElse("name", "")
        val url = dict.getOrElse("url", "")
        (nombre, url)
      }
    }
    // Si todo va bien saca la lista, si el archivo no existe devuelve lista vacía
    tryResult.getOrElse(List.empty)
  }

  // Dejamos esto temporalmente igual hasta llegar al Ejercicio 4
  def downloadFeed(urlOrPath: String): String = {
    val source = Source.fromURL(urlOrPath)
    source.mkString
  }
}
