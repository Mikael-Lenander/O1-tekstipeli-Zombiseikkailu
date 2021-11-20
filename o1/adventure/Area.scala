package o1.adventure

import scala.collection.mutable.Map
import scala.math._

/** The class `Area` represents locations in a text adventure game world. A game world
  * consists of areas. In general, an "area" can be pretty much anything: a room, a building,
  * an acre of forest, or something completely different. What different areas have in
  * common is that players can be located in them and that they can have exits leading to
  * other, neighboring areas. An area also has a name and a description.
  * @param name         the name of the area
  * @param description  a basic description of the area (typically not including information about items) */

abstract class Area(val name: String, val description: String, onLeave: (Area, Direction) => Unit=(_,_)=>{}) {

  protected val _neighbors = Map[Direction, Area]()

  def neighbors = this._neighbors
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

  def leave(player: Player, direction: Direction) = {
    onLeave(this, direction)
    ""
  }

  /** Adds exits from this area to the given areas. Calling this method is equivalent to calling
    * the `setNeighbor` method on each of the given direction--area pairs.
    * @param exits  contains pairs consisting of a direction and the neighboring area in that direction. */
  def setNeighbors(exits: Vector[(Direction, Area)]) = {
    this._neighbors ++= exits
  }

  def removeNeighbor(direction: Direction) = {
    this._neighbors.remove(direction)
  }

  def exitList = "\nVoit edetä suuntiin: " + this.neighbors.keys.mkString(" ") + "."

  def itemDescriptions = this.items.map(_._2.areaDescription).mkString(", ")

  def fullDescription: String

}

class PeacefulArea(name: String, description: String, onLeave: (Area, Direction) => Unit=(_,_)=>{}) extends Area(name, description, onLeave) {

  def zombieHorde = None

  def fullDescription = this.description + this.itemDescriptions + this.exitList
}

class ZombieArea(name: String, description: String, val zombieDescriptions: Vector[String], var zombieHorde: Option[ZombieHorde], player: Player, onLeave: (ZombieArea, Direction) => Unit=(_,_)=>{}) extends Area(name, description) {
  private var descriptionIndex = 0

  def fightingMethods = {
    def maxLoss(loss: Int) = min(loss, Player.MaxHealth)
    this.zombieHorde.filter(horde => horde.isClose && horde.numZombies <= 10).map(zombieHorde => {
      val run = if (this.neighbors.keys.exists(direction => zombieHorde.directions.contains(direction))) s"\nJos juokset zombien ohi, terveydentilasi heikkenee ${maxLoss(zombieHorde.runningHealthLoss)} yksikköä." else ""
      val stab = if (player.has("puukko")) s"\nJos puukotat zombit, terveydentilasi heikkenee ${maxLoss(Knife.healthLoss(zombieHorde.numZombies))} yksikköä." else ""
      val shoot = if (player.has("kivääri") && player.rifle.exists(_.hasAmmo)) s"\nJos ammut zombit kiväärillä, ne eivät pääse sinuun käsiksi. ${player.rifle.get.ammoLeftMessage}" else ""
      run + stab + shoot
    }).getOrElse("")
  }

  def eliminateZombieHorde() = {
    this.zombieHorde = None
  }

  override def leave(player: Player, direction: Direction) = {
    this.descriptionIndex = min(this.descriptionIndex + 1, this.zombieDescriptions.size - 1)
    val attackMgs = this.zombieHorde.filter(horde => horde.isClose && horde.isInDirection(direction)).map(_.attack(player)).getOrElse("")
    this.zombieHorde.foreach(_.approach())
    onLeave(this, direction)
    attackMgs
  }

  def zombieDescription = this.zombieHorde.map(horde => this.zombieDescriptions(descriptionIndex).replace("x", horde.numZombies.toString) + this.fightingMethods).getOrElse("")

  def fullDescription = {
    this.description + this.zombieDescription + this.exitList
  }
}

class CabinEntrance(player: Player) extends ZombieArea("Mökki", "Seisot mökin edessä.", Vector(" Mökin oven edessä parveilee x hengen zombilauma. Sinun on hoideltava ne, jotta pääset sisään."), Some(new ZombieHorde(5, 1, Vector(West))), player) {

  var isOpen = false

  def isZombieHorde = this.zombieHorde.exists(_.isClose)

  def open() = {
    if (!this.isZombieHorde) {
      this.isOpen = true
      true
    } else false
  }

  override def neighbors = if (this.isOpen) this._neighbors else this._neighbors.filter(_._2.name != "Sisällä mökissä")

  def hasKeyDescription = if (player.has("avain")) " Kokeile, sopiiko löytämäsi avain siihen." else " Kokeilet avata mökin oven, mutta se on lukossa. Ehkä lähistöltä löytyy avain siihen."

  override def fullDescription = {
    if (this.isZombieHorde) super.fullDescription else this.description + this.hasKeyDescription + this.exitList
  }

}

class Cabin(player: Player) extends Area("Sisällä mökissä", "Aika palata kotiin.") {
    def zombieHorde = None

    val openingMessage =
      """Olet sisällä mökissä. Tutkit talon läpikotaisin, kunnes päädyt kylpyhuoneeseen. Avaat peilikaapin ja löydät rokoteannoksia! Näillä ystäväsi pelastuvat!
        |Olet juuri poistumassa mökistä, kun kuulet selkäsi takana aseen latautuvan. Toinen selviytyjä osoittaa sinua selkään haulikolla.
        |Selviytyjä: "Mitä ihmettä teet minun kodissani?"
        |Miten reagoit tilanteeseen? Valitse 'a' tai 'b'.""".stripMargin

   val tree = new Root(
     openingMessage,
     Branch(
       Desicion("a", "Käänny hitaasti ympäri, ja selitä, ettet tiennyt mökin olevan asuttu."),
       "Selviytyjä: \"No nyt tiedät, että tämä talo on varattu. Lähde meneen, ennen kuin ammun sinut! Ja rokotteet jäävät tänne.\"",
       Leaf(
         Desicion("a", "Sano, että lähdet kyllä, mutta rokotteet tulevat joka tapauksessa mukaasi."),
         "Selviytyjä kyllästyy uhitteluusi ja ampuu sinut :(",
         true
       ),
       Branch(
         Desicion("b", "Anele, että saat ottaa edes osan rokotteista mukaasi, koska ystäväsi tarvitsevat niitä."),
         "Selviytyjä: \"Miksi minua pitäisi kiinnostaa sinun ystäväsi?\"",
         Leaf(
           Desicion("a", "Ehdota selviytyjälle, että hän tulee sinuun mukaasi selviytyjien kylään tapaamaan ystäviäsi. Siellä on paljon turvallisempaa kuin täällä mökissä."),
           "Selviytyjä suostuu ideaasi ja antaa sinulle rokottensa. Nyt voitte palata kotiin sankareina!",
           false
         ),
         Leaf(
           Desicion("b", "Heitä selviytyjää lampulla ja pakene ovesta ulos."),
           "Selviytyjä raivostuu ja ampuu sinut :(",
           true
         )
       )
     ),
     Leaf(
       Desicion("b", "Näet metrin päässä pöydällä pistoolin. Tartu siihen!"),
       "Selviytyjä huomaa aikeesi ja ampuu sinut :(",
       true
     )
   )

  var state: Tree = this.tree

  def finalBossFinished = this.state.isFinished

  def execute(input: String) = {
    this.state = this.state.options(input)
    if (this.finalBossFinished && this.state.isLosing) player.loseFinalBoss(this.state.fullDescription)
    else if (this.finalBossFinished) {
      player.get("rokote")
      this.state.fullDescription
    }
    else ""
  }

  def fullDescription = {
    if (!this.finalBossFinished) this.state.fullDescription else this.description + this.exitList
  }

}