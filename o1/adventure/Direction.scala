package o1.adventure

/** Direction-piirre kuvaa ilmansuuntia, joiden avulla pelaaja liikkuu alueiden välillä. */
sealed trait Direction {
  val direction: String
  override def toString = this.direction
}

object North extends Direction {
  val direction = "pohjoinen"
}

object East extends Direction {
  val direction = "itä"
}

object South extends Direction {
  val direction = "etelä"
}

object West extends Direction {
  val direction = "länsi"
}