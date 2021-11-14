package o1.adventure.ui
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine

case class Desicion(letter: String, description: String)

sealed trait Tree {
  val value: Desicion

  def initialize: String
  def execute: String
  }

case class Leaf(value: Desicion) extends Tree {
  def initialize = ""

  def execute = value.description
}
case class Branch(value: Desicion, left: Tree, right: Tree) extends Tree {
  val options = Map(left.value.letter -> left, right.value.letter -> right)

  def initialize = this.value.description + "\n" + Vector(left, right).map(node => s"${node.value.letter}: ${node.value.description}").mkString("\n")

  def execute = {
    val input = readLine()
    val child = this.options(input)
    println(child.initialize)
    child.execute
  }
}

class Root(left: Tree, right: Tree) extends Branch(Desicion("", ""), left, right)

object Test extends App {
   val tree = new Root(
     Branch(
       Desicion("a", "1"),
       Branch(
         Desicion("a", "11"),
         Leaf(Desicion("a", "111")),
         Leaf(Desicion("b", "112"))
       ),
       Branch(
         Desicion("b", "12"),
         Leaf(Desicion("a", "121")),
         Leaf(Desicion("b", "122"))
       )
     ),
     Branch(
       Desicion("b", "2"),
       Branch(
         Desicion("a", "21"),
         Leaf(Desicion("a", "211")),
         Leaf(Desicion("b", "212"))
       ),
       Branch(
         Desicion("b", "22"),
         Leaf(Desicion("a", "221")),
         Leaf(Desicion("b", "222"))
       )
     )
   )

  println(tree.initialize)
  println(tree.execute)

}
