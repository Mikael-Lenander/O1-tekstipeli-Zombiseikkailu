package o1.adventure
import scala.math.max

/** ZombieHorde-luokka mallintaa alueella olevaa zombielaumaa, jota vastaan pelaajan on taisteltava.
  * @param _numZombies zombien määrä laumassa. Mitä enemmän zombeja on, sitä vaikeampi niitä vastaan on taistella
  * @param distance zombilauman etäisyys alueesta. Tämä ei tarkoita, että zombit ovat toisella alueella. Jos etäisyys on yli 0, zombit ovat
  * vielä "kartan ulkopuolella", josta ne eivät voi käydä pelaajan kimppuun. Kun etäisyys on 0, zombilaumasta tulee pelaajalle uhka
  * @param directions suunnat, josta zombilauma hyökkää. Jos pelaaja menee eri suuntaan, zombilaumalla ei ole vaikutusta pelaajaan */
class ZombieHorde(private var _numZombies: Int, private var distance: Int, val directions: Vector[Direction]) {

  def numZombies: Int = this._numZombies

  def isClose: Boolean = this.distance == 0

  def isInDirection(direction: Direction): Boolean = this.directions.contains(direction)

  /** Kertoo, kuinka paljon pelaajan terveydentila heikkenee, jos se juoksee zombilauman ohi eli menee siihen suuntaan, jossa zombilauma on (this.directions). */
  def runningHealthLoss: Int = this._numZombies / 2

  /** Vähentää zombien määrää zombilaumassa pelaajan hyökköyksen seurauksena.
    * @param number zombien määrä, jotka pelaaja korkeintaan tappaa
    * @return jäljelle jäävien zombien määrä ja tapettujen zombien määrä */
  def killZombies(number: Int=_numZombies): Tuple2[Int, Int] = {
    val initNum = this._numZombies
    this._numZombies = max(0, this._numZombies - number)
    (this.numZombies, initNum - this._numZombies)
  }

  /** Zombit lähestyvät aluetta ja muodostavat uhan, kun etäisyys (this.distance) putoaa nollaan. */
  def approach() = {
    this.distance = max(this.distance - 1, 0)
  }

  /** Jos pelaaja liikkuu siihen suuntaan, missä zombit ovat (this.directions), zombilauma hyökkää pelaajan kimppuun, minkä seurauksena
    * pelaajan terveydentila heikkenee. */
  def attack(player: Player): String = {
    player.changeHealth(-this.runningHealthLoss)
    if (player.isAlive)
      s"\nHuh, pääsit zombien ohi, mutta ne onnistuivat raatelemaan sinua. Terveydentilasi heikkeni ${this.runningHealthLoss} yksikköä.\n"
    else
      ""
  }
}
