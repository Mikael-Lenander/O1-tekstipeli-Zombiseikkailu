package o1.adventure
import scala.math.ceil

import javax.tools.DocumentationTool.Location

/** The class `Item` represents items in a text adventure game. Each item has a name
  * and a  *  longer description. (In later versions of the adventure game, items may
  * have other features as well.)
  *
  * N.B. It is assumed, but not enforced by this class, that items have unique names.
  * That is, no two items in a game world have the same name.
  *
  * @param name         the item's name
  * @param description  the item's description */
abstract class Item(val name: String, val description: String, val areaDescription: String) {

  def use(player: Player): String

  /** Returns a short textual representation of the item (its name, that is). */
  override def toString = this.name

}

abstract class Weapon(name: String, description: String, areaDescription: String) extends Item(name, description, areaDescription) {

  def use(player: Player): String

  def noUseForWeaponMsg(name: String) = s"Alueella ei ole mitään, mihin käyttää $name."
}

object Knife extends Weapon("puukko", "Puukolla voit nirhata vastaantulevia zombeja. Koska joudut käyttämään puukkoa lähietäisyydeltä,\nzombit ehtivät yleensä vahingoittamaan sinua. Käytä siis puukkoa varoen!", " Tiskin takaa löytyy kuitenkin vielä puukko. Se ei ole paras mahdollinen ase, mutta saa luvan kelvata." + pickupInstrucion) {

  def healthLoss(zombiesKilled: Int) = ceil(zombiesKilled.toDouble / 4).toInt

  def stabZombies(player: Player, location: ZombieArea): String = {
    location.zombieHorde.map(horde => {
      val (_, zombiesKilled) = horde.killZombies()
      location.eliminateZombieHorde()
      player.changeHealth(-this.healthLoss(zombiesKilled))
      if (player.isAlive)
        s"Onnistuit puukottamaan kaikki zombit, mutta ne ehtivät raadella sinua. Terveydentilasi huononi ${this.healthLoss(zombiesKilled)} yksikköä.\n" + player.stateDescription
      else
        ""
    }).getOrElse(noUseForWeaponMsg("puukkoa"))
  }

  def use(player: Player) = {
    player.location match {
      case location: ZombieArea => this.stabZombies(player, location)
      case _ => noUseForWeaponMsg("puukkoa")
    }
  }
}

class Rifle extends Weapon("kivääri", "Kiväärillä voit ampua zombit turvallisesti kaukaa. Luoteja on vain seitsemän, joten käytä niitä säästeliäästi.", " Huomaat myös, ettö ojassa lepää kivääri! Tuolla zombien tappaminen lienee helpompaa.") {
  var ammunitionLeft = 7

  def hasAmmo = ammunitionLeft > 0

  def ammoLeftMessage = s"Sinulla on ${this.ammunitionLeft} luotia jäljellä."

  def shootBird(player: Player): String = {
    val bird = player.get("pöllö")
    bird.foreach(_ => this.ammunitionLeft -= 1)
    val birdMsg = bird.map(_ => s"Ammuit pöllön alas ja poimit sen. Nyt ei pitäisi ruoan loppua äkkiä :P. ${this.ammoLeftMessage}").getOrElse(noUseForWeaponMsg("kivääriä"))
    birdMsg + player.stateDescription
  }

  def shootZombies(location: ZombieArea, player: Player) = {
      location.zombieHorde.map(horde => {
        val (zombiesLeft, zombiesKilled) = horde.killZombies(ammunitionLeft)
        this.ammunitionLeft -= zombiesKilled
        if (zombiesLeft == 0) location.eliminateZombieHorde()
        if (zombiesLeft > 0) s"Voi ei! Ammukset loppuivat kesken. Zombeja on vielä $zombiesLeft jäljellä.\n" + player.stateDescription else s"Onnistuit ampumaan kaikki zombit. ${this.ammoLeftMessage}"
      }).getOrElse(noUseForWeaponMsg("kivääriä"))
  }

  def use(player: Player) = {
    if (!this.hasAmmo) "Sinulla ei ole enää ammuksia jäljellä."
    else player.location match {
      case location: PeacefulArea => this.shootBird(player)
      case location: ZombieArea => this.shootZombies(location, player)
      case _ => noUseForWeaponMsg("kivääriä")
    }
  }
}

class Food(name: String, description: String, areaDescription: String, energy: Int) extends Item(name, description, areaDescription) {

  def use(player: Player): String = {
    val fullnessIncrease = player.changeFullness(energy)
    player.removeItem(this.name)
    s"Nyt ei pitäisi olla enää nälkä. Kylläisyytesi nousi ${fullnessIncrease} yksikköä.\n" + player.stateDescription
  }
}

object Medkit extends Item("ensiapupakkaus", "Ensiapupakkauksella voit nostaa nopeasti terveydentilaasi.", " ja ensiapupakkauksen! Jos zombit pääsevät sinuun vielä käsiksi, tästä on hyötyä." + pickupInstrucion) {
  val healthIncrease = 2
  def use(player: Player): String = {
      val healthChange = player.changeHealth(healthIncrease)
      player.removeItem(this.name)
      s"Käytit ensiapupakkauksen. Terveydentilasi parani $healthChange yksikköä.\n" + player.stateDescription
  }
}

object Key extends Item("avain", "Ehkä avain sopii johonkin lähellä olevaan lukkoon.", " Maassa makaa kuollut selviytyjä. Onkohan hänellä jotain hyödyllisiä tarvikkeita? Tutkit selviytyjän taskut, ja sieltä löytyy avain. Poimi se!") {
  def use(player: Player): String = {
    player.location match {
      case location: CabinEntrance => {
        val isOpen = location.open()
        if (isOpen) {
          player.go(West)
          "Mökin ovi aukeaa. Menet sisään."
        } else {
          "Et voi avata ovea, koska se on zombien piirittämä."
        }
      }
      case _ => "Täällä ei taida olla mitään, mihin avainta voisi käyttää."
    }
  }
}

object Vaccine extends Item("rokote", "Rokotteilla saat pelastettua sairaat ystäväsi selviytyjien kylässä.", "") {
  def use(player: Player) = "Vie nämä selviytyjien kylään ja pelasta kaverisi."
}