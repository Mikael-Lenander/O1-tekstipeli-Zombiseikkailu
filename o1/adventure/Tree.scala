package o1.adventure

case class Desicion(letter: String, description: String)

sealed trait Tree {
  def desicion: Desicion

  def fullDescription: String

  def options: Map[String, Tree]

  def isFinished: Boolean

  def isLosing: Boolean
}

case class Leaf(desicion: Desicion, description: String, isLosing: Boolean) extends Tree {
  val options = Map()

  def fullDescription = this.description

  def isFinished = true
}
case class Branch(desicion: Desicion, description: String, left: Tree, right: Tree) extends Tree {
  val options = Map(left.desicion.letter -> left, right.desicion.letter -> right)

  def fullDescription = this.description + "\n" + Vector(left, right).map(node => s"${node.desicion.letter}: ${node.desicion.description}").mkString("\n")

  def isFinished = false
  def isLosing = false
}

class Root(description: String, left: Tree, right: Tree) extends Branch(Desicion("", ""), description, left, right)
