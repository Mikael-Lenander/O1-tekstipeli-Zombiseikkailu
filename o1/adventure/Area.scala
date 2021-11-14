package o1.adventure

import scala.collection.mutable.Map
import scala.math.min

/** The class `Area` represents locations in a text adventure game world. A game world
  * consists of areas. In general, an "area" can be pretty much anything: a room, a building,
  * an acre of forest, or something completely different. What different areas have in
  * common is that players can be located in them and that they can have exits leading to
  * other, neighboring areas. An area also has a name and a description.
  * @param name         the name of the area
  * @param description  a basic description of the area (typically not including information about items) */

abstract class Area(val name: String, val description: String, onLeave: (Area, Direction) => Unit=(_,_)=>{}) {

  val neighbors = Map[Direction, Area]()
 /* private */val items = Map[String, Item]()
  def zombieHorde: Option[ZombieHorde]

  def addItem(item: Item): Unit = {
    this.items(item.name) = item
  }

  def contains(itemName: String): Boolean = this.items.contains(itemName)

  def removeItem(itemName: String): Option[Item] = this.items.remove(itemName)

  /** Returns the area that can be reached from this area by moving in the given direction. The result
    * is returned in an `Option`; `None` is returned if there is no exit in the given direction. */
  def neighbor(direction: Direction): Option[Area] = this.neighbors.get(direction)


  /** Adds an exit from this area to the given area. The neighboring area is reached by moving in
    * the specified direction from this area. */
  def setNeighbor(direction: Direction, neighbor: Area) = {
    this.neighbors += direction -> neighbor
  }

  def leave(player: Player, direction: Direction) = {
    onLeave(this, direction)
    ""
  }

  /** Adds exits from this area to the given areas. Calling this method is equivalent to calling
    * the `setNeighbor` method on each of the given direction--area pairs.
    * @param exits  contains pairs consisting of a direction and the neighboring area in that direction
    * @see [[setNeighbor]] */
  def setNeighbors(exits: Vector[(Direction, Area)]) = {
    this.neighbors ++= exits
  }

  def removeNeighbor(direction: Direction) = {
    this.neighbors.remove(direction)
  }

  /** Returns a multi-line description of the area as a player sees it. This includes a basic
    * description of the area as well as information about exits and items. The return
    * value has the form "DESCRIPTION\n\nExits available: DIRECTIONS SEPARATED BY SPACES".
    * The directions are listed in an arbitrary order. */

  def exitList = "\nVoit edetä suuntiin: " + this.neighbors.keys.mkString(" ") + "."

  def itemDescriptions = this.items.map(_._2.areaDescription).mkString(", ")

  def fullDescription: String


  /** Returns a single-line description of the area for debugging purposes. */
  override def toString = this.name + ": " + this.description.replaceAll("\n", " ").take(150)

}

class PeacefulArea(name: String, description: String, onLeave: (Area, Direction) => Unit=(_,_)=>{}) extends Area(name, description, onLeave) {

  def zombieHorde = None

  def fullDescription = this.description + this.itemDescriptions + this.exitList
}

class ZombieArea(name: String, description: String, val zombieDescriptions: Vector[String], var zombieHorde: Option[ZombieHorde], onLeave: (ZombieArea, Direction) => Unit=(_,_)=>{}) extends Area(name, description) {
  private var descriptionIndex = 0

  def eliminateZombieHorde() = {
    this.zombieHorde = None
  }

  override def leave(player: Player, direction: Direction) = {
    this.descriptionIndex = min(this.descriptionIndex + 1, this.zombieDescriptions.size - 1)
    this.zombieHorde.foreach(_.approach())
    val attackMgs = this.zombieHorde.filter(_.isClose(direction)).map(_.attack(player)).getOrElse("")
    onLeave(this, direction)
    attackMgs
  }

  def fullDescription = {
    val zombieDescription = this.zombieHorde.map(horde => this.zombieDescriptions(descriptionIndex).replace("x", horde.numZombies.toString)).getOrElse("")
    this.description + zombieDescription + this.exitList
  }
}

class CabinEntrance(player: Player) extends Area("Mökki", "Seisot mökin edessä. Ehkä löytämäsi avain sopii lukkoon.") {
  val noKeyDescription = "Löydät metsästä mökin. Kokeilet avata oven, mutta se on lukossa. Ehkä lähistöltä löytyy avain siihen."

  def zombieHorde = None

  var isOpen = false

  def open() = {
    isOpen = true
  }

  private def visibleNeighbors = if (this.isOpen) this.neighbors else this.neighbors.filter(_._2.name != "Sisällä mökissä")

  override def exitList = "\nVoit edetä suuntiin: " + this.visibleNeighbors.keys.mkString(" ") + "."

  override def neighbor(direction: Direction): Option[Area] = this.visibleNeighbors.get(direction)

  def fullDescription = (if (player.has("avain")) this.description else this.noKeyDescription) + this.exitList
}

object Cabin extends Area("Sisällä mökissä", "") {
    def zombieHorde = None

  def fullDescription = this.description
}