package o1.adventure

sealed trait Direction {
  val direction: String
  override def toString = this.direction
}

object North extends Direction {
  val direction = "north"
}

object East extends Direction {
  val direction = "east"
}

object South extends Direction {
  val direction = "south"
}

object West extends Direction {
  val direction = "north"
}