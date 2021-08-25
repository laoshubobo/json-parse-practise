import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

class FileReaderTest extends AnyFunSuite with BeforeAndAfter {
  var reader: ActorRef = _
  val fileFormatStatisticMap = mutable.Map[String, mutable.Set[String]]()
  before {
    val filePath = "log_test1.json"
    val numOfWorkers = 4
    val actorSystem = ActorSystem("JSParseApplication")
    fileFormatStatisticMap.clear()
    reader = FileReader.apply(numOfWorkers, filePath, fileFormatStatisticMap, actorSystem)
  }

  test("File name are not same should be counted") {
    reader ! EntryRequest("filename1.php", "php")
    reader ! EntryRequest("filename2.php", "php")
    reader ! EntryRequest("filename2.java", "java")
    reader ! EntryRequest("filename3.txt", "txt")
    reader ! EntryRequest("filename4.cpp", "cpp")
    reader ! EntryRequest("filename5.cpp", "cpp")
    reader ! EntryRequest("filename6.cpp", "cpp")
    Thread.sleep(1000)
    val expectResult = List("php 2","java 1","txt 1","cpp 3")
    assert(getStatisticResult(fileFormatStatisticMap).sorted.equals(expectResult.sorted))
  }

  test("Empty extension entries should not be counted in the result") {
    reader ! EntryRequest("filename1", "")
    reader ! EntryRequest("filename2", "")
    reader ! EntryRequest("filename3", "")
    reader ! EntryRequest("filename4.php", "php")
    Thread.sleep(1000)
    val expectResult = List("php 1")
    assert(getStatisticResult(fileFormatStatisticMap).equals(expectResult))
  }

  test("Empty file name entries should not be counted in the result") {
    reader ! EntryRequest("", "java")
    reader ! EntryRequest("", "php")
    reader ! EntryRequest("filename.php", "php")
    Thread.sleep(1000)
    val expectResult = List("php 1")
    assert(getStatisticResult(fileFormatStatisticMap).equals(expectResult))
  }

  test("Same name files should count once") {
    // all files are same name
    reader ! EntryRequest("filename1", "txt")
    reader ! EntryRequest("filename1", "txt")
    reader ! EntryRequest("filename1", "txt")
    reader ! EntryRequest("filename1", "txt")
    Thread.sleep(1000)
    val expectResult = List("txt 1")
    assert(getStatisticResult(fileFormatStatisticMap).equals(expectResult))
  }
  private def getStatisticResult(fileFormatStatisticMap: mutable.Map[String, mutable.Set[String]]): List[String] = fileFormatStatisticMap.map(kv => kv._1 + " " + kv._2.size).toList
}
