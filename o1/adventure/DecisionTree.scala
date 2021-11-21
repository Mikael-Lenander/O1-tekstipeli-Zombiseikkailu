package o1.adventure

/** DecisionTree-piirre on pohja puurakenteelle, joka mallintaa pelaajan tekemeien päätösten ja niiden seurauksien muodostamaa hierarkiaa.
  * Puun solmut ovat seurauksia pelaajan päätöksistä ja polut useamman päätöksen sarjoja, jotka johtavat joko pelin voittamiseen tai häviämiseen.
  * Puurakennetta hyödynnetään pelin loppukohtauksessa, jossa pelaaja joutuu konfliktiin toisen selviytyjän kanssa. */
sealed trait DecisionTree {
  def decision: Decision

  def fullDescription: String

  /** Valinnat, jotka pelaaja voi tehdä */
  def options: Map[String, DecisionTree]

  /** Kertoo, onko solmu lehti */
  def isFinished: Boolean

  /** Kertoo, onko pelaaja tehnyt huonoja päätöksiä ja hävinnyt pelin. */
  def isLosing: Boolean
}

/** Desicion-luokka kuvaa pelaajan tekemää valintaa.
  * @param letter kirjain, jonka käyttäjä syöttää tehdäkseen tämän valinnan
  * @param description valinnan kuvaus*/
case class Decision(letter: String, description: String)


/** Leaf-luokka kuvaa lehteä eli puun päässä olevaa solmua. Mallintaa tässä viimeisen valinnan serausta.
  * @param decision päätös, joka johti tähän lehteen
  * @param isLosing kertoo, johtiko pelaajan tekemät valinnat pelin häviämiseen vai voittamiseen */
case class Leaf(decision: Decision, description: String, isLosing: Boolean) extends DecisionTree {
  val options = Map()

  def fullDescription = this.description

  def isFinished = true
}

/** Branch-luokka kuvaa solmua, joka haarautuu eteenpäin.
  * @param decision päätös, joka johti tähän solmuun
  * @param left solmun vasen haara
  * @param right solmun oikea haara */
case class Branch(decision: Decision, description: String, left: DecisionTree, right: DecisionTree) extends DecisionTree {
  val options = Map(left.decision.letter -> left, right.decision.letter -> right)

  def fullDescription = this.description + "\n\n" + Vector(left, right).map(node => s"${node.decision.letter}: ${node.decision.description}").mkString("\n")

  def isFinished = false
  def isLosing = false
}

class Root(description: String, left: DecisionTree, right: DecisionTree) extends Branch(Decision("", ""), description, left, right)
