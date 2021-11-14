package o1.adventure

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

  val noUseForWeaponMsg = "Alueella ei ole mitään, mihin käyttää asetta."
}

object Knife extends Weapon("puukko", "Puukolla voit nirhata zombeja lähietäisyydeltä. Huonona puolena joudut päästämään zombit kovin lähelle, joten saatat ottaa pientä osumaa.", " Tiskin takaa löytyy kuitenkin vielä puukko. Se ei ole paras mahdollinen ase, mutta saa luvan kelvata." + pickupInstrucion) {

  def stabZombies(player: Player, location: ZombieArea): String = {
    location.zombieHorde.map(horde => {
      val (_, zombiesKilled) = horde.killZombies()
      location.eliminateZombieHorde()
      val healthLoss = 1 + zombiesKilled / 4
      player.changeHealth(-healthLoss)
      if (player.isAlive)
        s"Onnistuit puukottamaan kaikki zombit, mutta ne ehtivät raadella sinua. Terveydentilasi huononi ${healthLoss} yksikköä." + player.stateDescription
      else
        ""
    }).getOrElse(noUseForWeaponMsg)
  }

  def use(player: Player) = {
    player.location match {
      case location: ZombieArea => this.stabZombies(player, location)
      case _ => noUseForWeaponMsg
    }
  }
}

object ShotGun extends Weapon("haulikko", "Haulikolla voit ampua zombit turvallisesti kaukaa. Luoteja on vain viisi, joten käytä niitä sääteliäästi.", " Huomaat myös, ettö ojassa lepää haulikko! Tuolla zombien tappaminen lienee helpompaa.") {
  var ammunitionLeft = 5

  def shootBird(player: Player): String = {
    val bird = player.get("pöllö")
    bird.foreach(_ => this.ammunitionLeft -= 1)
    val birdMsg = bird.map(_ => "Ammuit pöllön alas. Nyt ei pitäisi ruoan loppua äkkiä :P.").getOrElse(noUseForWeaponMsg)
    birdMsg + player.stateDescription
  }

  def shootZombies(location: ZombieArea) = {
      location.zombieHorde.map(horde => {
        val (zombiesLeft, zombiesKilled) = horde.killZombies(ammunitionLeft)
        this.ammunitionLeft -= zombiesKilled
        if (zombiesLeft == 0) location.eliminateZombieHorde()
        if (zombiesLeft > 0) s"Voi ei! Ammukset loppuivat kesken. Zombeja on vielä $zombiesLeft jäljellä." else "Onnistuit ampumaan kaikki zombit."
      }).getOrElse(noUseForWeaponMsg)
  }

  def use(player: Player) = {
    if (this.ammunitionLeft <= 0) "Sinulla ei ole enää ammuksia jäljellä."
    else player.location match {
      case location: PeacefulArea => this.shootBird(player)
      case location: ZombieArea => this.shootZombies(location)
    }
  }
}

class Food(name: String, description: String, areaDescription: String, energy: Int) extends Item(name, description, areaDescription) {

  def use(player: Player): String = {
    player.changeFullness(energy)
    player.removeItem(this.name)
    s"Nyt ei pitäisi olla enää nälkä. Kylläisyytesi nousi ${this.energy} yksikköä." + player.stateDescription
  }
}

object Medkit extends Item("ensiapupakkaus", "Ensiapupakkauksella voit nostaa nopeasti terveydentilaasi.", " ja ensiapupakkauksen! Jos zombit pääsevät sinuun vielä käsiksi, tästä on hyötyä." + pickupInstrucion) {
  val healthIncrease = 2
  def use(player: Player): String = {
      val healthChange = player.changeHealth(healthIncrease)
      player.removeItem(this.name)
      s"Käytit ensiapupakkauksen. Terveydentilasi parani $healthChange yksikköä." + player.stateDescription
  }
}

object Key extends Item("avain", "Ehkä avain sopii johonkin lähellä olevaan lukkoon.", " Maassa makaa kuollut selviytyjä. Onkohan hänellä jotain hyödyllisiä tarvikkeita? Tutkit selviytyjän taskut, ja sieltä löytyy avain.") {
  def use(player: Player): String = {
    player.location match {
      case location: CabinEntrance => {
        location.open()
        player.go(West)
        "Mökin ovi aukeaa. Menet sisään."
      }
      case _ => "Täällä ei taida olla mitään, mihin avainta voisi käyttää."
    }
  }
}