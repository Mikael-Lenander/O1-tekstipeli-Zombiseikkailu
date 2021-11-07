package o1.adventure


/** The class `Adventure` represents text adventure games. An adventure consists of a player and
  * a number of areas that make up the game world. It provides methods for playing the game one
  * turn at a time and for checking the state of the game.
  *
  * N.B. This version of the class has a lot of "hard-coded" information which pertain to a very
  * specific adventure game that involves a small trip through a twisted forest. All newly created
  * instances of class `Adventure` are identical to each other. To create other kinds of adventure
  * games, you will need to modify or replace the source code of this class. */
class Adventure {

  /** The title of the adventure game. */
  val title = "Zobmiseikkailu"

  private val survivorVillage = new Area("Selviytyjien kylä", Vector("Aika lähteä matkaan. Idässä on maantie, jota pitkin pääset eteenpäin.", "Olet takaisin selviytyjien kylässä. Tänne sinulla ei ole mitään asiaa ilman rokotteita."))
  private val road1 = new Area("Maantie", Vector("Kävelet maantietä pitkin kohti itää. Voit jatkaa eteenpäin, mutta etelässä on kaupunki lähellä. Sieltä voi löytyä hyödyllisiä tarvikkeita."))

  private val cityMsg = "Olet kaupungissa. Idässä näkyy ruokakauppa, etelässä asekauppa ja lännessä sairaala. Pois kaupungista pääsee menemällä pohjoiseen.\n"
  private val city = new Area("Kaupunki", Vector(cityMsg + "Horisontissa näkyy kuitenkin zombilauma. Ehdit käydä vain kahdessa paikassa ennen kuin zombit tulevat. Valitse siis tarkkaan, missä paikoissa haluat käydä.",
    cityMsg + "Ehdit käydä vielä yhdessä paikassa, ennen kuin zombit tulevat.",
    cityMsg + "Zombit ovat kuitenkin jo vallanneet kaupungin. Parasta lähteä takaisin, ennen kuin ne syövät sinut."),
    Some(new ZombieHorde(30, 3, Vector(East, South, West))))

  private val hospital = new Area("Sairaala", Vector("Olet sairaalassa. Näet ympärilläsi kymmenittäin ruumiita..."))

  private val weaponShop = new Area("Asekauppa", Vector("Olet asekaupassa. Valitettavasti hyllyt on tyhjennetty aikoja sitten."))
  private val groceryStore = new Area("Ruokakauppa", Vector("Olet ruokakaupassa. Kaikki jäljellä ovat ruoat näyttävät pilaantuneilta."))
  private val crossRoads = new Area("Risteys", Vector("Jatkat maantietiä eteenpäin. Saavut risteykseen. Pohjoisessa on suuri metsä. Maantie jatkuu etelään. Sieltä kuuluu vaimeaa zombien murinaa." +
    "\nEhkä ne vartioivat jotakin. Kumpaan suuntaan haluat mennä, valinta on sinun."))
  private val road2 = new Area("Maantie", Vector("Maantie jatkuu etelään. Zombien murina kuuluu nyt selkeämmin. Vielä on mahdollista kääntyä takaisin."))
  private val road3 = new Area("Maantie", Vector("Edessäsi on pieni zombilauma. Katsot taaksesi, ja huomaat olevasi zombien piirittämä! Toivottavasti sinulla on ase mukanasi. Voit yrittää juosta zombien ohi, mutta se on hyvin vaarallista..."), Some(new ZombieHorde(3, 0, Vector(South))))
  private val weaponStash = new Area("Maantien pää", Vector("Olet saapunut maantien päähän. Näet metsäpolun, joka johtaa pohjoiseen suureen metsään."))
  private val forest1 = new Area("Metsä", Vector("Sisäänkäynti"))
  private val forest2 = new Area("Metsä", Vector("Tyhjää"))
  private val forest3 = new Area("Metsä", Vector("Pöllö"))
  private val forest4 = new Area("Metsä", Vector("Zombeja"), Some(new ZombieHorde(5, 0, Vector(North))))
  private val forest5 = new Area("Metsä", Vector("Avain!"))
  private val survivorsHome = new Area("Selviytyjän koti", Vector(""))
  private val destination = survivorVillage

  survivorVillage.setNeighbors(Vector(East -> road1))
            road1.setNeighbors(Vector(East -> crossRoads, South -> city))
             city.setNeighbors(Vector(North -> crossRoads, East -> groceryStore, South -> weaponShop, West -> hospital))
         hospital.setNeighbors(Vector(East -> city))
       weaponShop.setNeighbors(Vector(North -> city))
     groceryStore.setNeighbors(Vector(West -> city))
       crossRoads.setNeighbors(Vector(North -> forest1, South -> road2))
            road2.setNeighbors(Vector(North -> crossRoads, South -> road3))
            road3.setNeighbors(Vector(South -> weaponStash))
      weaponStash.setNeighbors(Vector(North -> forest1))
          forest1.setNeighbors(Vector(North -> forest2, East -> forest3, South -> crossRoads, West -> survivorsHome))
          forest2.setNeighbors(Vector(East -> forest4, South -> forest1))
          forest3.setNeighbors(Vector(North -> forest4, West -> forest1))
          forest4.setNeighbors(Vector(North -> forest5, South -> forest3, West -> forest2))
          forest5.setNeighbors(Vector(South -> forest4))
    survivorsHome.setNeighbors(Vector(East -> forest1))

  val player = new Player(survivorVillage)

  hospital.addItem(Medkit)
  weaponShop.addItem(Knife)
  groceryStore.addItem(new Food("myslipatukka", "Pitäisi pitää nälän loitolla - ainakin hetken.", "\nPitkän tonkimisen jälkeen löydät hyllyn alta avaamattoman myslipatukan :P.", 5))
  weaponStash.addItem(ShotGun)
  forest3.addItem(new Food("pöllö", "Tästä pitäisi riittää ruokaa pitkäksi aikaa :P.", "", 10))
  forest5.addItem(Key)

  /** The number of turns that have passed since the start of the game. */
  var turnCount = 0
  /** The maximum number of turns that this adventure game allows before time runs out. */
  val timeLimit = 40


  /** Determines if the adventure is complete, that is, if the player has won. */
  def isComplete = {
    this.player.location == this.destination && this.player.has("vaccine")
  }

  /** Determines whether the player has won, lost, or quit, thereby ending the game. */
  def isOver = this.isComplete || this.player.hasQuit || !player.isAlive

  /** Returns a message that is to be displayed to the player at the beginning of the game. */
  def welcomeMessage =
    """
      |Olet yksi selviytyjistä zombimaailmassa. Asut selviytyjien kylässä. Osaa kyläläisitä on purtu, ja he muuttuvat zombeiksi vuorokauden
      |sisällä. Onneksi zombivirukseen on kehitetty rokote. Sinun on löydettävä rokoteannoksia, tai muuten ystäväsi kuolevat. Muista pitää
      |myös huolta itsestäsi. Alla olevista palkeista näet kylläisyytesi ja terveydentilasi. Älä anna niiden kulua loppuun, tai olet mennyttä.
      |""".stripMargin


  /** Returns a message that is to be displayed to the player at the end of the game. The message
    * will be different depending on whether or not the player has completed their quest. */
  def goodbyeMessage = {
    if (this.isComplete)
      "Home at last... and phew, just in time! Well done!"
    else if (!this.player.isAlive)
      "Kuolit :("
    else  // game over due to player quitting
      "Quitter!"
  }


  /** Plays a turn by executing the given in-game command, such as "go west". Returns a textual
    * report of what happened, or an error message if the command was unknown. In the latter
    * case, no turns elapse. */
  def playTurn(command: String) = {
    val action = new Action(command)
    val outcomeReport = action.execute(this.player)
    if (outcomeReport.isDefined) {
      this.turnCount += 1
    }
    outcomeReport.getOrElse("Unknown command: \"" + command + "\".")
  }


}

