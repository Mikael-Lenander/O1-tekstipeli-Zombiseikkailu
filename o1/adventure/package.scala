package o1

package object adventure {
  // Vakiot
  val Directions = Map("pohjoinen" -> North, "itä" -> East, "etelä" -> South, "länsi" -> West)
  val Instructions =
    """mene 'ilmansuunta': Liiku valitsemaasi ilmansuuntaan. Vaihtoehtoisesti voit jättää mene-sanan pois ja kirjoittaa vain ilmansuunnan nimen
      |käytä 'esine': käytä mitä tahansa poimimaasi esinettä
      |poimi 'esine': poimi alueelta löytyvä esine
      |tutki 'esine': näyttää esineen ominaisuudet
      |apua: listaa kaikki komennot
      |lopeta: lopettaa pelin""".stripMargin
  val pickupInstrucion = "\nVoit poimia esineen komennolla: poimi 'esine'."
  val weaponInstruction = "\nVoit käyttää poimimiasi aseita komennolla: käytä 'ase'."
}
