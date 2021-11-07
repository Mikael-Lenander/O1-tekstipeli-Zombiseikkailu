package o1.adventure

import scala.collection.mutable.Map
import scala.math._


/** A `Player` object represents a player character controlled by the real-life user of the program.
  *
  * A player object's state is mutable: the player's location and possessions can change, for instance.
  *
  * @param startingArea  the initial location of the player */
object Player {
  val MaxHealth = 5
  val MaxFullness = 25
}

class Player(startingArea: Area) {

  private var currentLocation = startingArea        // gatherer: changes in relation to the previous location
  private var quitCommandGiven = false              // one-way flag
  private val items = Map[String, Item]()
  private var health = Player.MaxHealth
  private var fullness = Player.MaxFullness

  def changeHealth(number: Int) = {
    if (number > 0)
      this.health = min(this.health + number, Player.MaxHealth)
    else
      this.health = max(this.health + number, 0)
    println(s"elämät: ${this.health}")
  }

  def changeFullness(number: Int) = {
    if (number > 0)
      this.fullness = min(this.fullness + number, Player.MaxFullness)
    else
      this.fullness = max(this.fullness - number, 0)
  }

  def isAlive = this.fullness > 0 && this.health > 0

  def examine(itemName: String): String =
    this.items.get(itemName).map(_.description).getOrElse(s"Sinulla ei ole esinettä '$itemName'.")

  def get(itemName: String): String = {
      val item = this.location.removeItem(itemName)
      item.foreach(item => this.items(item.name) = item)
      println(this.location.items)
      item.map(item => s"Poimit esineen '${item.name}'. ${item.description}").getOrElse(s"Alueella ei ole esinettä $itemName")
  }

  def selectItem(itemName: String): Option[Item] = this.items.get(itemName)

  def has(itemName: String): Boolean = this.items.contains(itemName)

  def inventory: String =
    if (this.items.nonEmpty) s"You are carrying:\n${this.items.keys.mkString("\n")}" else "You are empty-handed."

  /** Determines if the player has indicated a desire to quit the game. */
  def hasQuit = this.quitCommandGiven


  /** Returns the current location of the player. */
  def location = this.currentLocation

  /** Attempts to move the player in the given direction. This is successful if there
    * is an exit from the player's current location towards the direction name. Returns
    * a description of the result: "You go DIRECTION." or "You can't go DIRECTION." */
  def go(direction: Direction) = {
    val destination = this.location.neighbor(direction)
    // msg kertoo, miten alueelta lähteminen mahdollisesti vaikutti pelaajan tilaan.
    val msg = destination.map(_ => this.currentLocation.leave(this, direction)).getOrElse("")
    this.currentLocation = destination.getOrElse(this.currentLocation)
    if (destination.isDefined) msg else "Et voi mennä suuntaan " + direction + "."
  }

  /** Signals that the player wants to quit the game. Returns a description of what happened within
    * the game as a result (which is the empty string, in this case). */
  def quit() = {
    this.quitCommandGiven = true
    ""
  }

  /** Returns a brief description of the player's state, for debugging purposes. */
  override def toString = "Now at: " + this.location.name


}


