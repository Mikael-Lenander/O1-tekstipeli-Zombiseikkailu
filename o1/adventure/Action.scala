package o1.adventure


/** The class `Action` represents actions that a player may take in a text adventure game.
  * `Action` objects are constructed on the basis of textual commands and are, in effect,
  * parsers for such commands. An action object is immutable after creation.
  * @param input  a textual in-game command such as "go east" or "rest" */
class Action(input: String) {

  private val commandText = input.trim.toLowerCase
  private val verb        = commandText.takeWhile( _ != ' ' ).toLowerCase
  private val modifiers   = commandText.drop(verb.length).trim.toLowerCase


  /** Causes the given player to take the action represented by this object, assuming
    * that the command was understood. Returns a description of what happened as a result
    * of the action (such as "You go west."). The description is returned in an `Option`
    * wrapper; if the command was not recognized, `None` is returned. */
  def execute(actor: Player): Option[String] = {
    this.verb match {
    case "mene" => Directions.get(this.modifiers).map(actor.go(_))
    case "pohjoinen" | "etelä" | "itä" | "länsi" => Directions.get(this.verb).map(actor.go(_))
    case "käytä" => actor.selectItem(this.modifiers).map(_.use(actor))
    case "poimi" => Some(actor.pick(this.modifiers.toLowerCase))
    case "tutki" => Some(actor.examine(this.modifiers))
    case "apua" => Some(Instructions)
    case "quit"  => Some(actor.quit())
    case other   => None
  }
  }


  /** Returns a textual description of the action object, for debugging purposes. */
  override def toString = this.verb + " (modifiers: " + this.modifiers + ")"


}

