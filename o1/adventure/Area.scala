package o1.adventure

import scala.collection.mutable.Map
import scala.math._

/** Area-luokka on runko tarkemmille alueiden määrittelyille.
  * @param name alueen nimi
  * @param description alueen lyhyt kuvaus
  * @param onLeave suorittaa mahdollisen tilamuutoksen, joka aiheutuu alueelta lähtemisestä */
abstract class Area(val name: String, val description: String, onLeave: (Area, Direction) => Unit=(_,_)=>{}) {

  protected val _neighbors = Map[Direction, Area]()

  def neighbors = this._neighbors

  private val items = Map[String, Item]()

  /** Jokaiselle alueelle on määritelty, sisältääkö se zombilauman. */
  def zombieHorde: Option[ZombieHorde]

  def addItem(item: Item): Unit = {
    this.items(item.name) = item
  }

  def contains(itemName: String): Boolean = this.items.contains(itemName)

  def removeItem(itemName: String): Option[Item] = this.items.remove(itemName)

  def neighbor(direction: Direction): Option[Area] = this.neighbors.get(direction)

  /** Kutsuu onLeave-parametrifunktiota, joka toteuttaa mahdollisen tilamuutoksen alueelta poistuessa. Esimerkiksi joillakin
    * alueilla voi käydä vain kerran, jolloin onLeave voi poistaa alueelta naapurin.
    * @return tyhjä merkkijono */
  def leave(player: Player, direction: Direction): String = {
    onLeave(this, direction)
    ""
  }

  def setNeighbors(exits: Vector[(Direction, Area)]): Unit = {
    this._neighbors ++= exits
  }

  def removeNeighbor(direction: Direction): Option[Area] = {
    this._neighbors.remove(direction)
  }

  def exitList: String = "\nVoit edetä suuntiin: " + this.neighbors.keys.mkString(" ") + "."

  def itemDescriptions: String = this.items.map(_._2.areaDescription).mkString(", ")

  /** Alueen kuvaus määräytyy sen mukaan, millaisesta alueesta on kyse. */
  def fullDescription: String

}

/** Alue, jossa ei ole zombeja. */
class PeacefulArea(name: String, description: String, onLeave: (Area, Direction) => Unit=(_,_)=>{}) extends Area(name, description, onLeave) {

  def zombieHorde = None

  def fullDescription: String = this.description + this.itemDescriptions + this.exitList
}

/** Alue, jossa on zombeja tai ei ole, koska pelaaja on tappanut ne.
  * @param zombieDescriptions kuvaukset, jotka liittyvät zombilaumaan. Joillain alueilla on useampia kuvauksia, koska zombilauman tila voi muuttua.
  * @param zombieHorde mahdollinen zombilauma Option-käöreessä */
class ZombieArea(name: String, description: String, val zombieDescriptions: Vector[String], var zombieHorde: Option[ZombieHorde], val player: Player, onLeave: (ZombieArea, Direction) => Unit=(_,_)=>{}) extends Area(name, description) {

  private var descriptionIndex = 0 // indeksi, jolla valitaan oikea zombieDescription

  /** Listaa käyttäjälle yksityiskohtaisesti tavat, jolla se voi taistella zombilaumaa vastaan. */
  def fightingMethods: String = {
    def maxLoss(loss: Int) = min(loss, Player.MaxHealth)
    this.zombieHorde.filter(horde => horde.isClose && horde.numZombies <= 10).map(zombieHorde => {
      val run = if (this.neighbors.keys.exists(direction => zombieHorde.directions.contains(direction))) s"\nJos juokset zombien ohi, terveydentilasi heikkenee ${maxLoss(zombieHorde.runningHealthLoss)} yksikköä." else ""
      val stab = if (player.has("puukko")) s"\nJos puukotat zombit, terveydentilasi heikkenee ${maxLoss(Knife.healthLoss(zombieHorde.numZombies))} yksikköä." else ""
      val shoot = if (player.has("kivääri") && player.rifle.exists(_.hasAmmo)) s"\nJos ammut zombit kiväärillä, ne eivät pääse sinuun käsiksi. ${player.rifle.get.ammoLeftMessage}" else ""
      run + stab + shoot
    }).getOrElse("")
  }

  /** Poistaa zombilauman alueelta. Tämä tapahtuu, kun pelaaja tuhoaa zombilauman. */
  def eliminateZombieHorde(): Unit = {
    this.zombieHorde = None
  }

  /** Muuttaa pelaajan ja alueen tilaa alueelta lähtiessä. Jos zombilauman etäisyys (distance, ks. ZombieHorde-luokka) on yli 0, pienentää etäisyyttä ja muuttaa
    * zombieDescriptionia sen mukaan. Jos etäisyys on 0 ja pelaaja liikkuu samaan suuntaan kuin mistä zombilauma tulee, lauma hyökkää palaajan kimppuun.
    * @return mahdollinen viesti, joka kertoo, miten pelaajan tila muuttui */
  override def leave(player: Player, direction: Direction): String = {
    this.descriptionIndex = min(this.descriptionIndex + 1, this.zombieDescriptions.size - 1)
    val attackMgs = this.zombieHorde.filter(horde => horde.isClose && horde.isInDirection(direction)).map(_.attack(player)).getOrElse("")
    this.zombieHorde.foreach(_.approach())
    onLeave(this, direction)
    attackMgs
  }

  /** Palauttaa kuvauksen, joka kertoo zombilauman tilasta, ja miten sitä vastaan voi taistella. */
  def zombieDescription: String =
    this.zombieHorde.map(horde => this.zombieDescriptions(descriptionIndex).replace("x", horde.numZombies.toString) + this.fightingMethods).getOrElse("")

  def fullDescription: String = this.description + this.zombieDescription + this.exitList
}

/** Alue, josta pääsee mökkiin (Cabin) eli pelin loppukohtaukseen. Pelaaja tarvitsee avaimen, jotta tämän alueen kautta pääsee mökkiin sisälle. */
class CabinEntrance(player: Player) extends ZombieArea("Mökki", "Seisot mökin edessä.", Vector(" Mökin oven edessä parveilee x hengen zombilauma. Sinun on hoideltava ne, jotta pääset sisään."), Some(new ZombieHorde(5, 1, Vector(West))), player) {

  var isOpen = false // Kertoo, onko mökin ovi auki eli pääseekö mökkiin sisälle.

  def isZombieHorde: Boolean = this.zombieHorde.exists(_.isClose)

  /** Avaa ovet mökkiin (Cabin). Avaaminen onnituu vain, jos edessä ei ole zombilaumaa.
    * @return true, jos oven avaaminen onnistui, muuten false */
  def open(): Boolean = {
    if (!this.isZombieHorde) {
      this.isOpen = true
      true
    } else false
  }

  /** Jos pelaajalla on avain (Key), tältä alueelta pääsee mökkiin. Muuten mökki ei kuulu alueen naapureihin. */
  override def neighbors = if (this.isOpen) this._neighbors else this._neighbors.filter(_._2.name != "Sisällä mökissä")

  /** Palauttaa viestin, joka antaa pelaajalle vihjeitä avaimesta. */
  def hasKeyDescription = if (player.has("avain")) " Kokeile, sopiiko löytämäsi avain siihen." else " Kokeilet avata mökin oven, mutta se on lukossa. Ehkä lähistöltä löytyy avain siihen."

  override def fullDescription =
    if (this.isZombieHorde) super.fullDescription else this.description + this.hasKeyDescription + this.exitList
}

/** Pelin loppukohtaus, joka poikkeaa suuresti muista alueista. Sisältää puurakenteen (ks. DecisionTree), joka mallintaa keskustelua toisen selviytyjän kanssa.
  * Alueella on suostuteltava selviytyjää antamaan rokotteitaan, joilla pelaaja voi pelastaa ystävänsä. */
class Cabin(player: Player) extends Area("Sisällä mökissä", "Aika palata kotiin.") {
    def zombieHorde = None

    val openingMessage =
      """Olet sisällä mökissä. Tutkit talon läpikotaisin, kunnes päädyt kylpyhuoneeseen. Avaat peilikaapin ja löydät rokoteannoksia! Näillä ystäväsi pelastuvat!
        |Olet juuri poistumassa mökistä, kun kuulet selkäsi takana aseen latautuvan. Toinen selviytyjä osoittaa sinua selkään haulikolla.
        |Selviytyjä: "Mitä ihmettä teet minun kodissani?"
        |Miten reagoit tilanteeseen? Valitse 'a' tai 'b'.""".stripMargin

   /** Sisältää keskustelun selviytyjän kanssa ja kaikki päätökset, joita pelaaja voi tehdä keskustelun aikana. */
   val desicionTree = new Root(
     openingMessage,
     Branch(
       Decision("a", "Käänny hitaasti ympäri, ja selitä, ettet tiennyt mökin olevan asuttu."),
       "Selviytyjä: \"No nyt tiedät, että tämä talo on varattu. Lähde meneen, ennen kuin ammun sinut! Ja rokotteet jäävät tänne.\"",
       Leaf(
         Decision("a", "Sano, että lähdet kyllä, mutta otat rokotteet mukaasi, ja avaat ulko-oven."),
         "Selviytyjä ei anna sinun varastaa rokotteita ja ampuu sinut :(",
         true
       ),
       Branch(
         Decision("b", "Anele, että saat ottaa edes osan rokotteista mukaasi, koska ystäväsi tarvitsevat niitä."),
         "Selviytyjä: \"Miksi minua pitäisi kiinnostaa sinun ystäväsi?\"",
         Leaf(
           Decision("a", "Ehdota selviytyjälle, että hän tulee sinuun mukaasi selviytyjien kylään tapaamaan ystäviäsi. Siellä on paljon turvallisempaa kuin täällä mökissä."),
           "Selviytyjä suostuu ideaasi ja antaa sinulle rokotteensa. Nyt voitte palata kotiin sankareina!",
           false
         ),
         Leaf(
           Decision("b", "Heitä selviytyjää lampulla ja juokse."),
           "Selviytyjä raivostuu ja ampuu sinut :(",
           true
         )
       )
     ),
     Leaf(
       Decision("b", "Näet kahden metrin päässä pöydällä pistoolin. Tartu siihen!"),
       "Selviytyjä huomaa aikeesi ja ampuu sinut :(",
       true
     )
   )

  /** Varastoi keskustelun tilan eli puun solmun, johon pelaajan aikaisemmat päätöksen ovat johtaneet. */
  var node: DecisionTree = this.desicionTree

  /** Kertoo, onko keskustelu ohi. */
  def finalBossFinished: Boolean = this.node.isFinished

  /** Vie loppukohtausta eteenpäin pelaajan tekemän valinnan perusteella eli etenee puun seuraavaan solmuun. Pelaaja voi joko hävitä tai voittaa
    * pelin (päätymällä puun lehteen) tai jatkaa keskustelua selviytyjän kanssa eteenpäin. */
  def execute(input: String): String = {
    this.node = this.node.options(input)
    if (this.finalBossFinished && this.node.isLosing) player.loseFinalBoss(this.node.fullDescription)
    else if (this.finalBossFinished) {
      player.get("rokote")
      this.node.fullDescription
    }
    else ""
  }

  def fullDescription = {
    if (!this.finalBossFinished) this.node.fullDescription else this.description + this.exitList
  }

}