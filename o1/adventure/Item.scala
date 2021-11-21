package o1.adventure
import scala.math.ceil

/** Item-luokka on runko esineille, joita pelaaja voi kerätä pelin aikana.
  * @param name esineen nimi
  * @param description esineen tarkempi kuvaus
  * @param areaDescription kuvaus, joka näkyy alueen (Area) kuvauksessa ja kertoo esineestä pelaajalle */
abstract class Item(val name: String, val description: String, val areaDescription: String) {

  /** Pelaaja voi käyttää kaikkia esineitä jollain tavalla. */
  def use(player: Player): String

  override def toString = this.name
}

/** Kuvaa esinettä, jolla pelaaja voi taistella zombeja vastaan. */
abstract class Weapon(name: String, description: String, areaDescription: String) extends Item(name, description, areaDescription) {

  def use(player: Player): String

  def noUseForWeaponMsg(name: String): String = s"Alueella ei ole mitään, mihin käyttää $name."
}

/** Puukko-olio */
object Knife extends Weapon("puukko", "Puukolla voit nirhata vastaantulevia zombeja. Koska joudut käyttämään puukkoa lähietäisyydeltä,\nzombit ehtivät yleensä vahingoittamaan sinua. Käytä siis puukkoa varoen!", " Tiskin takaa löytyy kuitenkin vielä puukko. Se ei ole paras mahdollinen ase, mutta saa luvan kelvata." + pickupInstrucion) {

  /** Kertoo, kuinka paljon pelaajan terveydentila heikkenee puukkoa käyttäessä (kuitenkin vähemmän kuin jos ei käytä asetta ollenkaan). */
  def healthLoss(zombiesKilled: Int): Int = ceil(zombiesKilled.toDouble / 4).toInt

  /** Pelaaja hyökkää puukon kanssa alueella olevan zombilauman kimppuun.
    * @return viesti, joka kertoo, mitä taistelussa tapahtui */
  def stabZombies(player: Player, location: ZombieArea): String = {
    location.zombieHorde.filter(_.isClose).map(horde => {
      val (_, zombiesKilled) = horde.killZombies()
      location.eliminateZombieHorde()
      player.changeHealth(-this.healthLoss(zombiesKilled))
      if (player.isAlive)
        s"Onnistuit puukottamaan kaikki zombit, mutta ne ehtivät raadella sinua. Terveydentilasi huononi ${this.healthLoss(zombiesKilled)} yksikköä.\n" + player.stateDescription
      else
        ""
    }).getOrElse(noUseForWeaponMsg("puukkoa"))
  }

  def use(player: Player): String = {
    player.location match {
      case location: ZombieArea => this.stabZombies(player, location)
      case _ => noUseForWeaponMsg("puukkoa")
    }
  }
}

/** Kivääri-luokka */
class Rifle extends Weapon("kivääri", "Kiväärillä voit ampua zombit turvallisesti kaukaa. Luoteja on vain seitsemän, joten käytä niitä säästeliäästi.", " Huomaat myös, ettö ojassa lepää kivääri! Tuolla zombien tappaminen lienee helpompaa.") {
  var ammunitionLeft = 7 // Kiväärissä on rajallinen määrä luoteja

  def hasAmmo: Boolean = ammunitionLeft > 0

  def ammoLeftMessage: String = s"Sinulla on ${this.ammunitionLeft} luotia jäljellä."

  def shootBird(player: Player): String = {
    val bird = player.get("pöllö")
    bird.foreach(_ => this.ammunitionLeft -= 1)
    val birdMsg = bird.map(_ => s"Ammuit pöllön alas ja poimit sen. Nyt ei pitäisi ruoan loppua äkkiä :P. ${this.ammoLeftMessage}").getOrElse(noUseForWeaponMsg("kivääriä"))
    birdMsg + player.stateDescription
  }

  def shootZombies(location: ZombieArea, player: Player): String = {
      location.zombieHorde.filter(_.isClose).map(horde => {
        val (zombiesLeft, zombiesKilled) = horde.killZombies(ammunitionLeft)
        this.ammunitionLeft -= zombiesKilled
        if (zombiesLeft == 0) location.eliminateZombieHorde()
        if (zombiesLeft > 0) s"Voi ei! Ammukset loppuivat kesken. Zombeja on vielä $zombiesLeft jäljellä.\n" + player.stateDescription else s"Onnistuit ampumaan kaikki zombit. ${this.ammoLeftMessage}"
      }).getOrElse(noUseForWeaponMsg("kivääriä"))
  }

  def use(player: Player): String = {
    if (!this.hasAmmo) "Sinulla ei ole enää ammuksia jäljellä."
    else player.location match {
      case location: PeacefulArea => this.shootBird(player)
      case location: ZombieArea => this.shootZombies(location, player)
      case _ => noUseForWeaponMsg("kivääriä")
    }
  }
}

/** Food-luokka kuvaa ruokia, jotka nostavat pelaajan kylläisyyttä.
  * @param energy määrä, kuinka paljon pelaajan kylläisyys nousee */
class Food(name: String, description: String, areaDescription: String, energy: Int) extends Item(name, description, areaDescription) {

  def use(player: Player): String = {
    val fullnessIncrease = player.changeFullness(energy)
    player.removeItem(this.name)
    s"Nyt ei pitäisi olla enää nälkä. Kylläisyytesi nousi ${fullnessIncrease} yksikköä.\n" + player.stateDescription
  }
}

/** Medkit-olio kuvaa ensiapupakkausta, joka parantaa pelaajan terveydentilaa */
object Medkit extends Item("ensiapupakkaus", "Ensiapupakkauksella voit paikata pahimmat haavat. Nostaa terveydentilaasi 2 yksikköä.", "Täällä ei näyttäisi olevan rokotteita, mutta löydät ensiapupakkauksen. Jos zombit pääsevät sinuun vielä käsiksi, tästä on hyötyä." + pickupInstrucion) {
  val healthIncrease = 2

  def use(player: Player): String = {
      val healthChange = player.changeHealth(healthIncrease)
      player.removeItem(this.name)
      s"Käytit ensiapupakkauksen. Terveydentilasi parani $healthChange yksikköä.\n" + player.stateDescription
  }
}

/** Key-olio kuvaa avainta, jota pelaaja tarvitsee päästäkseen rokotteita sisältävään mökkiin. */
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

/** Vaccine-olio kuvaa rokotteita, jotka pelaajan on tarkoitus pelissä löytää. Rokotteilla ei voi tehdä mitään paitsi ne voi poimia. */
object Vaccine extends Item("rokote", "Rokotteilla saat pelastettua sairaat ystäväsi selviytyjien kylässä.", "") {
  def use(player: Player) = "Vie nämä selviytyjien kylään ja pelasta kaverisi."
}