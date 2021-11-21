package o1.adventure

/** Action-luokka kuvaa komentoja, joita pelaaja voi tehdä pelissä */
class Action(input: String) {

  private val commandText = input.trim.toLowerCase
  private val verb        = commandText.takeWhile( _ != ' ' ).toLowerCase
  private val modifiers   = commandText.drop(verb.length).trim.toLowerCase
  private val invalidCommand = "Tuntematon komento: \"" + this.commandText + "\"."

  def execute(actor: Player): String = this.verb match {
    case "mene" => Directions.get(this.modifiers).map(actor.go(_)).getOrElse(invalidCommand)
    case "pohjoinen" | "etelä" | "itä" | "länsi" => Directions.get(this.verb).map(actor.go(_)).getOrElse(invalidCommand)
    case "käytä" => actor.selectItem(this.modifiers).map(_.use(actor)).getOrElse(s"Sinulla ei ole esinettä '${this.modifiers}'.")
    case "poimi" => actor.pick(this.modifiers)
    case "tutki" => actor.examine(this.modifiers)
    case "varustus" => actor.inventory
    case "apua" => Instructions
    case "lopeta"  => actor.quit()
    case other   => invalidCommand
  }

  /** Kuvaa pelin loppukohtauksen komentoja, jotka ovat erit kuin pelissä muuten. */
  def executeFinalBoss(cabin: Cabin): String = this.verb match {
    case "a" | "b" => cabin.execute(verb)
    case _ => "Syötä a tai b."
  }

}

