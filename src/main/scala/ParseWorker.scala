import akka.actor.{Actor, ActorRef}
import org.slf4j.LoggerFactory
import net.liftweb.json._

class ParseWorker(reader: ActorRef) extends Actor {
  implicit val formats: DefaultFormats.type = DefaultFormats
  private val logger = LoggerFactory.getLogger(getClass.getSimpleName)

  override def preStart(): Unit = println(s"Worker ${this.self.path} actor started")

  override def postStop(): Unit = println(s"Worker ${this.self.path} actor stopped")

  def process(line: String): Option[Entry] = {
    try {
      Option.apply(parse(line).extract[Entry])
    } catch {
      case e: Exception =>
        logger.error(s"Cannot parse the Json string $e")
        None
    }
  }

  override def receive: Receive = {
    case EntryProcessing => reader ! EntryRequest()
    case line: String =>
      val entryOption: Option[Entry] = process(line)
      val fileNameOpt = entryOption.map(e => e.nm)
      val fileName = fileNameOpt.getOrElse("")
      val extension = fileName.lastIndexOf('.') match {
          case idx if idx > 0 => fileName.substring(idx + 1)
          case _ => ""
        }
      reader ! EntryRequest(fileName, extension)
  }
}

case class Entry(ts: String = "", pt: String = "", si: String = "", uu: String = "", bg: String = "", sha: String = "", nm: String = "", ph: String = "", dp: String = "")