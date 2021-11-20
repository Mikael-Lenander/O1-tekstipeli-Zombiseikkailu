package o1.adventure.ui
import scala.math._

object Test extends App {
  val MaxFullness = 25
  val MaxDisplay = 5
  var fullness = MaxFullness
  def fullnessRate = ceil(fullness * 1.0 / (MaxFullness / MaxDisplay)).toInt
  while (fullness >= 0) {
    println(s"fullness: $fullness")
    println(s"fullnessRate: $fullnessRate")
    fullness -= 1
  }
}
