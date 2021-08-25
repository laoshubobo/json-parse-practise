import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.io.Source
import scala.language.postfixOps

class FileReader(numberOfWorkers: Int, filePath: String, fileFormatStatisticMap: mutable.Map[String, mutable.Set[String]]) extends Actor {
  private val logger = LoggerFactory.getLogger(getClass.getSimpleName)
  private var count = 0
  private val source = Source.fromFile(getClass.getResource(filePath).getFile)
  private val iteratorOfSource = source.getLines()

  override def preStart(): Unit = {
    logger.info("FileReader actor started")
  }

  override def postStop(): Unit = {
    logger.info("FileReader actor stopped")
    source.close()
    context.stop(self)
  }

  private def getLine: String = {
    iteratorOfSource.next()
  }

  override def receive: Receive = {
    case EntryRequest(fileName, extension) =>
      if (fileName.nonEmpty && extension.nonEmpty) {
        fileFormatStatisticMap.put(extension, fileFormatStatisticMap.getOrElseUpdate(extension, mutable.Set(fileName)) += fileName)
      }
      if (iteratorOfSource.hasNext) {
        sender() ! getLine
      } else {
        sender() ! PoisonPill
        endOfReadingCheck()
      }
  }

  private def endOfReadingCheck(): Unit = {
    count = count + 1
    if (count == numberOfWorkers) {
      fileFormatStatisticMap.foreach(kv => logger.info(kv._1 + " " + kv._2.size))
      context.stop(self)
      context.system.terminate()
    }
  }
}

object FileReader extends App {
  private val filePath = "log.json"
  private val numOfWorkers = 4
  private val actorSystem = ActorSystem("JSParseApplication")
  private val map = mutable.Map[String, mutable.Set[String]]()
  private val reader = actorSystem.actorOf(Props(classOf[FileReader], numOfWorkers, filePath, map), "FileReader")

  private def prepareWorkers(numberOfWorkers: Int): Seq[ActorRef] = {
    for (id <- 1 to numberOfWorkers) yield actorSystem.actorOf(Props(classOf[ParseWorker], reader), s"worker-$id")
  }

  def apply(numberOfWorkers: Int, filePath: String, fileFormatStatisticMap: mutable.Map[String, mutable.Set[String]], actorSystem: ActorSystem): ActorRef =
    actorSystem.actorOf(Props(classOf[FileReader], numOfWorkers, filePath, fileFormatStatisticMap), "FileReader")

  prepareWorkers(numOfWorkers).foreach {
    _ ! EntryProcessing
  }
}

case class EntryRequest(fileName: String = "", extension: String = "")

case object EntryProcessing