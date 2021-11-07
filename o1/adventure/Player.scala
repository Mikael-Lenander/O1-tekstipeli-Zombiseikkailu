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
      this.health = max(this.health - number, 0)
  }

  def changeFullness(number: Int) = {
    if (number > 0)
      this.fullness = min(this.fullness + number, Player.MaxFullness)
    else
      this.fullness = max(this.fullness - number, 0)
  }

  def drop(itemName: String): String = {
    val dropped = this.items.remove(itemName)
    dropped.foreach(item => this.location.addItem(item))
    dropped.map(item => s"You drop the ${item.name}.").getOrElse("You don't have that!")
  }

  def examine(itemName: String): String = this.items.get(itemName)
    .map(item => s"You look closely at the ${item.name}.\n${item.description}")
    .getOrElse("If you want to examine something, you need to pick it up first.")

  def get(itemName: String): String = {
      val item = this.location.removeItem(itemName)
      item.foreach(item => this.items(item.name) = item)
      item.map(item => s"You pick up the ${item.name}.").getOrElse(s"There is no $itemName here to pick up.")
  }

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
  def go(direction: String) = {
    val destination = this.location.neighbor(direction)
    this.currentLocation = destination.getOrElse(this.currentLocation)
    if (destination.isDefined) "You go " + direction + "." else "You can't go " + direction + "."
  }


  /** Causes the player to rest for a short while (this has no substantial effect in game terms).
    * Returns a description of what happened. */
  def rest() = {
    "You rest for a while. Better get a move on, though."
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


