import FileReader.{actorSystem, reader}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

class BlackBoxTest extends AnyFunSuite with BeforeAndAfter {
  var reader: ActorRef = _
  val fileFormatStatisticMap = mutable.Map[String, mutable.Set[String]]()
  before{
    fileFormatStatisticMap.clear()
  }

  test("Same file name but different extension files should be counted") {
    val filePath = "log_test1.json"
    val numOfWorkers = 4
    val actorSystem = ActorSystem("JSParseApplication")
    reader = FileReader.apply(numOfWorkers, filePath, fileFormatStatisticMap, actorSystem)
    for (_ <- 1 to numOfWorkers) yield actorSystem.actorOf(Props(classOf[ParseWorker], reader)) ! EntryProcessing
    Thread.sleep(1000)
    val expectResult = List("qxd 1", "jpg 1", "hqx 1", "avi 1", "dbf 1")
    assert(getStatisticResult(fileFormatStatisticMap).sorted.equals(expectResult.sorted))
  }

  test("Same file name entries should be counted once") {
    val filePath = "log_test2.json"
    val numOfWorkers = 4
    val actorSystem = ActorSystem("JSParseApplication")
    reader = FileReader.apply(numOfWorkers, filePath, fileFormatStatisticMap, actorSystem)
    for (_ <- 1 to numOfWorkers) yield actorSystem.actorOf(Props(classOf[ParseWorker], reader)) ! EntryProcessing
    Thread.sleep(1000)
    val expectResult = List("fm3 1","mac 1")
    assert(getStatisticResult(fileFormatStatisticMap).sorted.equals(expectResult.sorted))
  }

  test("All unique extensions and the number of unique filenames should all be counted") {
    val filePath = "log_test3.json"
    val numOfWorkers = 4
    val actorSystem = ActorSystem("JSParseApplication")
    reader = FileReader.apply(numOfWorkers, filePath, fileFormatStatisticMap, actorSystem)
    for (_ <- 1 to numOfWorkers) yield actorSystem.actorOf(Props(classOf[ParseWorker], reader)) ! EntryProcessing
    Thread.sleep(1000)
    println(getStatisticResult(fileFormatStatisticMap).sorted)
    println()
    val expectResult = List("fm3 1", "mac 1", "cad 1", "mp3 1", "flv 1")
    assert(getStatisticResult(fileFormatStatisticMap).sorted.equals(expectResult.sorted))
  }

  test("Invalid entry should not be counted") {
    val filePath = "log_test4.json"
    val numOfWorkers = 4
    val actorSystem = ActorSystem("JSParseApplication")
    reader = FileReader.apply(numOfWorkers, filePath, fileFormatStatisticMap, actorSystem)
    for (_ <- 1 to numOfWorkers) yield actorSystem.actorOf(Props(classOf[ParseWorker], reader)) ! EntryProcessing
    Thread.sleep(1000)
    assert(getStatisticResult(fileFormatStatisticMap).isEmpty)
  }


  private def getStatisticResult(fileFormatStatisticMap: mutable.Map[String, mutable.Set[String]]): List[String] = fileFormatStatisticMap.map(kv => kv._1 + " " + kv._2.size).toList

}
