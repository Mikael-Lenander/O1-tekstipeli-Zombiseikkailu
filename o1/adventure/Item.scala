package o1.adventure

/** The class `Item` represents items in a text adventure game. Each item has a name
  * and a  *  longer description. (In later versions of the adventure game, items may
  * have other features as well.)
  *
  * N.B. It is assumed, but not enforced by this class, that items have unique names.
  * That is, no two items in a game world have the same name.
  *
  * @param name         the item's name
  * @param description  the item's description */
abstract class Item(val name: String, val description: String, val player: Player) {

  def use(): Unit

  /** Returns a short textual representation of the item (its name, that is). */
  override def toString = this.name

}

class Weapon(name: String, description: String, player: Player, val longRange: Boolean) extends Item(name, description, player) {
  var durability = if (longRange) 5 else 999

  def use(): Unit = {}
}

class Food(name: String, description: String, player: Player, energy: Int) extends Item(name, description, player) {

  def use(): Unit = {}
}

class Medkit(player: Player, healthIncrease: Int) extends Item("Ensiapupakkaus", "Kuvaus", player) {
  def use(): Unit = {}
}

class Key(player: Player) extends Item("avain", "Avain!", player) {
  def use(): Unit = {}
}