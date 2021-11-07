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
class Area(val name: String, val descriptions: Vector[String], private var _zombieHorde: Option[ZombieHorde]=None) {

  private val neighbors = Map[Direction, Area]()
 /* private */val items = Map[String, Item]()
  // Alueen kuvaus on eri riippuen siitä, monettako kertaa pelaaja vierailee alueella.
  private var descriptionIndex = 0

  def addItem(item: Item): Unit = {
    this.items(item.name) = item
  }

  def contains(itemName: String): Boolean = this.items.contains(itemName)

  def removeItem(itemName: String): Option[Item] = this.items.remove(itemName)

  def zombieHorde = this._zombieHorde

  def killZombieHorde() = {
    this._zombieHorde = None
  }

  /** Returns the area that can be reached from this area by moving in the given direction. The result
    * is returned in an `Option`; `None` is returned if there is no exit in the given direction. */
  def neighbor(direction: Direction): Option[Area] = this.neighbors.get(direction)


  /** Adds an exit from this area to the given area. The neighboring area is reached by moving in
    * the specified direction from this area. */
  def setNeighbor(direction: Direction, neighbor: Area) = {
    this.neighbors += direction -> neighbor
  }

  def leave(player: Player, direction: Direction): String = {
    this.descriptionIndex = min(this.descriptionIndex + 1, this.descriptions.size - 1)
    this._zombieHorde.foreach(_.approach())
    val attackMgs = this._zombieHorde.filter(_.isClose(direction)).map(_.attack(player)).getOrElse("")
    attackMgs
  }

  /** Adds exits from this area to the given areas. Calling this method is equivalent to calling
    * the `setNeighbor` method on each of the given direction--area pairs.
    * @param exits  contains pairs consisting of a direction and the neighboring area in that direction
    * @see [[setNeighbor]] */
  def setNeighbors(exits: Vector[(Direction, Area)]) = {
    this.neighbors ++= exits
  }


  /** Returns a multi-line description of the area as a player sees it. This includes a basic
    * description of the area as well as information about exits and items. The return
    * value has the form "DESCRIPTION\n\nExits available: DIRECTIONS SEPARATED BY SPACES".
    * The directions are listed in an arbitrary order. */
  def fullDescription = {
    val exitList = "\nVoit edetä suuntiin: " + this.neighbors.keys.mkString(" ")
    val itemDescrption = this.items.map(_._2.areaDescription).mkString(", ")
    this.descriptions(descriptionIndex) + itemDescrption + exitList
  }


  /** Returns a single-line description of the area for debugging purposes. */
  override def toString = this.name + ": " + this.descriptions(descriptionIndex).replaceAll("\n", " ").take(150)

}

class ZombieArea(name: String, descriptions: Vector[String], private var _zombieHorde: Option[ZombieHorde]=None) {

}