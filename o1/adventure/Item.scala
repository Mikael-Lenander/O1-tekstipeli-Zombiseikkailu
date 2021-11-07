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
}

object Knife extends Weapon("puukko", "Puukolla voit nirhata zombeja lähietäisyydeltä. Huonona puolena joudut päästämään zombit kovin lähelle...", " Tiskin takaa löytyy kuitenkin vielä puukko. Se ei ole paras mahdollinen ase, mutta saa luvan kelvata.") {

  def use(player: Player) = {
    player.location.zombieHorde.map(horde => {
      val (_, zombiesKilled) = horde.killZombies()
      val healthLoss = 1 + zombiesKilled / 4
      player.changeHealth(-healthLoss)
      if (player.isAlive)
        s"Onnistuit puukottamaan kaikki zombit, mutta ne ehtivät raadella sinua. Terveydentilasi huononi ${healthLoss} yksikkiöä"
      else
        ""
    }).getOrElse("Alueella ei ole zombeja, joihin käyttää asetta.")
  }
}

object ShotGun extends Weapon("haulikko", "Haulikolla voit ampua zombit turvallisesti kaukaa. Luoteja on vain viisi, joten käytä niitä sääteliäästi.", " Huomaat myös, ettö ojassa lepää haulikko! Tuolla zombien tappaminen lienee helpompaa.\"") {
  var ammunitionLeft = 5
  def use(player: Player) = {
    if (ammunitionLeft <= 0) "Sinulla ei ole enää ammuksia jäljellä."
    else {
      player.location.zombieHorde.map(horde => {
        val (zombiesLeft, zombiesKilled) = horde.killZombies(ammunitionLeft)
        ammunitionLeft -= zombiesKilled
        if (zombiesLeft > 0) s"Voi ei! Ammukset loppuivat kesken. Zombeja on vielä $zombiesLeft jäljellä." else "Onnistuit ampumaan kaikki zombit"
      }).getOrElse("Alueella ei ole zombeja, joihin käyttää asetta.")
    }
  }
}

class Food(name: String, description: String, areaDescription: String, energy: Int) extends Item(name, description, areaDescription) {

  def use(player: Player): String = {""}
}

object Medkit extends Item("ensiapupakkaus", "Kuvaus", " ja ensiapupakkauksen! Jos zombit pääsevät sinuun vielä käsiksi, tästä on hyötyä.") {
  def use(player: Player): String = {""}
}

object Key extends Item("avain", "Avain!", "") {
  def use(player: Player): String = {""}
}