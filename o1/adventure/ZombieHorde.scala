package o1.adventure
import scala.math.max

class ZombieHorde(private var _numZombies: Int, private var distance: Int, private val directions: Vector[Direction]) {

  def numZombies = this._numZombies

  def isClose(direction: Direction) = this.distance == 0 && this.directions.contains(direction)

  // Palauttaa (zombien määrä, tapettujen zombien määrä)
  def killZombies(number: Int=_numZombies): Tuple2[Int, Int] = {
    val initNum = this._numZombies
    this._numZombies = max(0, this._numZombies - number)
    (this.numZombies, initNum - this._numZombies)
  }

  def approach() = {
    this.distance = max(this.distance - 1, 0)
    println(s"etäisyys ${distance}")
  }

  def attack(player: Player): String = {
    val healthLoss = this._numZombies / 2
    player.changeHealth(-healthLoss)
    if (player.isAlive)
      s"Huh, pääsit zombien ohi, mutta he onnistuivat raatelemaan sinua. Terveydentilasi heikkeni ${if (healthLoss == 1) "hiukan" else "merkittävästi"}."
    else
      ""
  }
}
