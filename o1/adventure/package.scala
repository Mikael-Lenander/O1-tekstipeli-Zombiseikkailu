package o1

import scala.io.AnsiColor._

package object adventure {
  // Vakiot
  val Directions = Map("pohjoinen" -> North, "itä" -> East, "etelä" -> South, "länsi" -> West)
  val Instructions =
    s"""mene 'ilmansuunta': Liiku valitsemaasi ilmansuuntaan. ${GREEN_B}Vaihtoehtoisesti voit jättää mene-sanan pois ja kirjoittaa vain ilmansuunnan nimen${RESET}
      |käytä 'esine': käytä mitä tahansa poimimaasi esinettä
      |poimi 'esine': poimi alueelta löytyvä esine
      |tutki 'esine': näyttää esineen ominaisuudet
      |varustus: näyttää pelaajan keräämät esineet
      |apua: listaa kaikki komennot
      |lopeta: lopettaa pelin""".stripMargin
  val pickupInstrucion = "\nVoit poimia esineen komennolla: poimi 'esine'."
  val weaponInstruction = "\nVoit käyttää poimimiasi aseita komennolla: käytä 'ase'."
}
