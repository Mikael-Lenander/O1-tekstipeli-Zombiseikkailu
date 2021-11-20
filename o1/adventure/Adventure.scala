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
  val title = "Zombiseikkailu"

  private val survivorVillage = new PeacefulArea("Selviytyjien kylä", "Aika lähteä matkaan. Idässä on maantie, jota pitkin pääset eteenpäin.")
  private val forest1 = new PeacefulArea("Metsä", "Olet metsän eteläreunassa. Metsästä saattaa löytyä jotain hyödyllistä. Lännestä pilkottaa valoa. Mistäköhän se tulee?")
  val player = new Player(forest1)

  private val road1 = new PeacefulArea("Maantie", "Kävelet maantietä pitkin kohti itää. Voit jatkaa eteenpäin, mutta etelässä on kaupunki lähellä. Sieltä voi löytyä hyödyllisiä tarvikkeita.")
  private val city = new ZombieArea("Kaupunki", "Olet kaupungissa. Idässä näkyy ruokakauppa, etelässä asekauppa ja lännessä sairaala. Pois kaupungista pääsee menemällä pohjoiseen.\n",
    Vector("Horisontissa näkyy kuitenkin zombilauma. Ehdit käydä vain kahdessa paikassa ennen kuin zombit tulevat. Valitse siis tarkkaan, missä paikoissa haluat käydä.",
           "Ehdit käydä vielä yhdessä paikassa, ennen kuin zombit tulevat.",
           "Zombit ovat jo vallanneet kaupungin. Parasta lähteä takaisin, ennen kuin ne syövät sinut."),
    Some(new ZombieHorde(30, 2, Vector(East, South, West))), player)
  private val hospital = new PeacefulArea("Sairaala", "Olet sairaalassa. Näet ympärilläsi kymmenittäin ruumiita...")
  private val weaponShop = new PeacefulArea("Asekauppa", "Olet asekaupassa. Hyllyt on tyhjennetty aikoja sitten.")
  private val groceryStore = new PeacefulArea("Ruokakauppa", "Olet ruokakaupassa. Kaikki jäljellä ovat ruoat näyttävät pilaantuneilta.")
  private val crossRoads = new PeacefulArea("Risteys", "Saavut risteykseen. Pohjoisessa on suuri metsä. Maantie jatkuu etelään. Sieltä kuuluu vaimeaa zombien murinaa." +
    "\nEhkä ne vartioivat jotakin. Kumpaan suuntaan haluat mennä, valinta on sinun.")
  private val road2 = new PeacefulArea("Maantie", "Maantie jatkuu etelään. Zombien murina kuuluu nyt selkeämmin. Vielä on mahdollista kääntyä takaisin.")
  private val road3 = new ZombieArea("Maantie", "Maantie jatkuu etelään. Loppu häämöttää jo.", Vector(" Edessäsi on pieni zombilauma. Katsot taaksesi, ja huomaat olevasi zombien piirittämä! Toivottavasti sinulla on ase mukana..." + weaponInstruction), Some(new ZombieHorde(4, 0, Vector(South))), player)
  private val weaponStash = new PeacefulArea("Maantien pää", "Olet saapunut maantien päähän. Näet metsäpolun, joka johtaa pohjoiseen suureen metsään.", (_, _) => forest1.removeNeighbor(South))
  private val forest2 = new PeacefulArea("Metsä", "Olet keskellä metsää.")
  private val forest3 = new PeacefulArea("Metsä", "Kuljet pitkin metsän itäreunaa.")
  private val forest4 = new ZombieArea("Metsä", "Kuljet pitkin metsän itäreunaa.", Vector(" Pohjoisessa näet kuolleen selviytyjän makaavan maassa. Ehkä hänellä on jotain arvokasta. Pohjoisessa on kuitenkin x hengen zombilauma."),
    Some(new ZombieHorde(10, 0, Vector(North))), player, (area, direction) => if (direction == North) area.eliminateZombieHorde())
  private val forest5 = new PeacefulArea("Metsä", "Olet metsän synkimmässä nurkassa.")
  private val destination = survivorVillage

  private val cabinEntrance = new CabinEntrance(player)
  private val cabin = new Cabin(player)

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
          forest1.setNeighbors(Vector(North -> forest2, East -> forest3, South -> crossRoads, West -> cabinEntrance))
          forest2.setNeighbors(Vector(East -> forest4, South -> forest1))
          forest3.setNeighbors(Vector(North -> forest4, West -> forest1))
          forest4.setNeighbors(Vector(North -> forest5, South -> forest3, West -> forest2))
          forest5.setNeighbors(Vector(South -> forest4))
    cabinEntrance.setNeighbors(Vector(East -> forest1, West -> cabin))
            cabin.setNeighbors(Vector(West -> survivorVillage))

  hospital.addItem(Medkit)
  weaponShop.addItem(Knife)
  groceryStore.addItem(new Food("patukka", "Patukan pitäisi pitää nälän loitolla - ainakin hetken. Lisää kylläisyyttäsi 1 yksikköä.", "\nPitkän tonkimisen jälkeen löydät hyllyn alta avaamattoman patukan :P." + pickupInstrucion, 5))
  weaponStash.addItem(new Rifle)
  forest3.addItem(new Food("pöllö", "Tästä pitäisi riittää ruokaa pitkäksi aikaa :P. Lisää kylläisyyttäsi 2 yksikköä.", " Näet pöllön tähystelevän puun latvustossa. Jos sinulla sattuisi olemaan kivääri mukana, pöllöstä saisi hyvän lounaan..." + weaponInstruction, 10))
  forest5.addItem(Key)
  cabin.addItem(Vaccine)

  /** Determines if the adventure is complete, that is, if the player has won. */
  def isComplete = {
    this.player.location == this.destination && this.player.has("rokote")
  }

  /** Determines whether the player has won, lost, or quit, thereby ending the game. */
  def isOver = this.isComplete || this.player.hasQuit || !player.isAlive || player.isStarved || player.finalBossLost

  /** Returns a message that is to be displayed to the player at the beginning of the game. */
  def welcomeMessage = {
    """
      |Olet yksi selviytyjistä zombimaailmassa. Asut selviytyjien kylässä. Osaa kyläläisitä on purtu, ja he muuttuvat zombeiksi vuorokauden
      |sisällä. Onneksi zombivirukseen on kehitetty rokote. Sinun on löydettävä rokoteannoksia, tai muuten ystäväsi kuolevat. Muista pitää
      |myös huolta itsestäsi. Alla olevista palkeista näet kylläisyytesi ja terveydentilasi. Älä anna niiden kulua loppuun, tai olet mennyttä.
      |""".stripMargin + player.stateDescription + "\n\nPelin komennot ovat:\n" + Instructions
  }


  /** Returns a message that is to be displayed to the player at the end of the game. The message
    * will be different depending on whether or not the player has completed their quest. */
  def goodbyeMessage = {
    if (this.isComplete)
      "Onneksi olkoon! Pääsit takaisin selviytyjien kotiin rokotteiden kanssa ja pelastit ystäväsi :)"
    else if (!this.player.isAlive)
      "Zombeja oli liikaa. Ne söivät sinut :("
    else if (this.player.isStarved) {
      "Et muistanut syödä ja kuolit nälkään :("
    }
    else if (this.player.finalBossLost) {
      "Hävisit pääpahikselle"
    }
    else  // game over due to player quitting
      "Luovuttaja!"
  }


  /** Plays a turn by executing the given in-game command, such as "go west". Returns a textual
    * report of what happened, or an error message if the command was unknown. In the latter
    * case, no turns elapse. */
  def playTurn(command: String) = {
    val action = new Action(command)
    player.location match {
      case cabin: Cabin => {
        if (!cabin.finalBossFinished)
          action.executeFinalBoss(cabin)
        else
            action.execute(this.player)
      }
      case _ => action.execute(this.player)
    }
  }


}

