package o1.adventure

import scala.collection.mutable.Map
import scala.math._
import io.AnsiColor._


/** Pelaaja-luokan companion object, varastoi muutamia vakioita. */
object Player {
  val MaxHealth = 5
  val MaxFullness = 20
  val MaxDisplay = 5 // Vaikka pelaajan elämät/kylläisyys olisi enemmän kuin MaxDisplay, käyttöliittymä näyttää ne tämän suuruisena asteikkona
}

/** Pelaaja-luokka, joka sisältää pelihahmon tilan.
  * @param startingArea alue, josta peli alkaa */
class Player(val startingArea: Area) {

  private var currentLocation = startingArea
  private var quitCommandGiven = false
  private var lostFinalBoss = false
  private val items = Map[String, Item]()
  private var health = Player.MaxHealth
  private var fullness = Player.MaxFullness

  /** Muuttaa pelaajan terveydentilaa (this.health) ylös- tai alaspäin. Jos terveydentila laskee nollaan, peli päättyy.
    * @return terveydentilan muutos */
  def changeHealth(number: Int): Int = {
    val current = this.health
    if (number > 0)
      this.health = min(this.health + number, Player.MaxHealth)
    else
      this.health = max(this.health + number, 0)
    this.health - current
  }

  /** Muuttaa pelaajan kylläisyyttä (this.fullness) ylös- tai alaspäin. Jos kylläisyys laskee nollaan, peli päättyy.
    * @return kylläisyyden muutos käyttöliittymäskaalassa */
  def changeFullness(number: Int): Int = {
    val current = this.fullness
    if (number > 0) {
      this.fullness = min(this.fullness + number, Player.MaxFullness)
    } else {
      this.fullness = max(this.fullness + number, 0)
    }
    ceil((fullness - current) * 1.0 / Player.MaxDisplay).toInt
  }

  /** Näyttää pelaajan tilaa koskevaa oleellista tietoa. */
  def stateDescription: String = {
    if (!this.isAlive || (this.location == this.startingArea && this.has("rokote"))) return ""
    val fullnessRate = ceil(fullness * 1.0 / (Player.MaxFullness / Player.MaxDisplay)).toInt
    val hungerMessage = if (this.fullness <= Player.MaxFullness / Player.MaxDisplay) s"\n${RED_B}Sinulla alkaa olla kova nälkä. Muista syödä. Voit syödä poimimiasi ruokia komennolla: käytä 'ruoka'.${RESET}" else ""
    val healthMessage = if (this.health <= 0.4 * Player.MaxHealth) s"\n${RED_B}Terveydentilasi on melko heikko. Toivottavasti sinulla on ensiapupakkaus mukanasi.${RESET}" else ""
    val inventory = if (this.items.nonEmpty) this.inventory else ""
    s"\nKylläisyytesi: ${"\uD83C\uDF57" * fullnessRate + "_" * (Player.MaxDisplay - fullnessRate)}\nTerveydentilasi: ${"♥" * health + "_" * (Player.MaxDisplay - health)}" + hungerMessage + healthMessage + inventory
  }


  def isAlive: Boolean = this.health > 0

  def isStarved: Boolean = this.fullness <= 0

  def finalBossLost: Boolean = this.lostFinalBoss

  def examine(itemName: String): String =
    this.items.get(itemName).map(_.description).getOrElse(s"Sinulla ei ole esinettä '$itemName'.")

  def get(itemName: String): Option[Item] = {
      val item = this.location.removeItem(itemName)
      item.foreach(item => this.items(item.name) = item)
      item
  }

  def pick(itemName: String): String = {
    val item = this.get(itemName)
    item.map(item => s"Poimit esineen '${item.name}'. ${item.description}").getOrElse(s"Alueella ei ole esinettä $itemName")
  }

  def selectItem(itemName: String): Option[Item] = this.items.get(itemName)

  def removeItem(itemName: String): Option[Item] = this.items.remove(itemName)

  def has(itemName: String): Boolean = this.items.contains(itemName)

  /** Palauttaa pelaajan kiväärin, jos pelaajalla on sellainen. */
  def rifle: Option[Rifle] = this.selectItem("kivääri") match {
    case Some(rifle: Rifle) => Some(rifle)
    case _ => None
  }

  def inventory: String =
    if (this.items.nonEmpty) s"\nSinulla on mukana: ${this.items.keys.mkString(", ")}." else "Sinulla ei ole esineitä."

  def hasQuit = this.quitCommandGiven

  def location = this.currentLocation

  /** Siirtää pelaajan toiselle alueelle. Samalla pelaajan kylläisyys pienenee yhden yksikön. Pelaajan nälkä siis kasvaa liikkuessa.
    * Kutsuu myös Area.leave-metodia, joka saattaa vaikuttaa pelaajan tai alueen tilaan.
    * @return pelaajan tilakuvaus tai ilmoitus, ettei valittuun suuntaan voi mennä */
  def go(direction: Direction) = {
    this.changeFullness(-1)
    val destination = this.location.neighbor(direction)
    val msg = destination.map(_ => this.currentLocation.leave(this, direction)).getOrElse("")
    this.currentLocation = destination.getOrElse(this.currentLocation)
    if (destination.isDefined) msg + this.stateDescription else "Et voi mennä suuntaan " + direction + "."
  }

  def quit() = {
    this.quitCommandGiven = true
    ""
  }

  /** Muuttaa pelaajan tilaa niin, että Adventure-olio tietää pelaajan hävinneen loppukohtauksen.
    * @return viesti, joka kertoo, mitä pelaaja teki väärin */
  def loseFinalBoss(message: String) = {
    this.lostFinalBoss = true
    message
  }
}


