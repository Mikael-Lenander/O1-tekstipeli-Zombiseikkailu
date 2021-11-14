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
  }

  def attack(player: Player): String = {
    val healthLoss = this._numZombies / 2
    player.changeHealth(-healthLoss)
    if (player.isAlive)
      s"\nHuh, pääsit zombien ohi, mutta ne onnistuivat raatelemaan sinua. Terveydentilasi heikkeni ${healthLoss} yksikköä."
    else
      ""
  }
}
