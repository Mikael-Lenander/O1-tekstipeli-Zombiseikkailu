package o1.adventure.ui
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine

object Test extends App {
  println("Tässä kestää hetki")
  var text = ""
  scala.concurrent.Future {
    Thread.sleep(3000)
   }.onComplete(_ => println(s"teksti $text"))
  text = readLine("Kirjoita x tai häviät: ")

}
