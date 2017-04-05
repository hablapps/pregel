package org.hablapps.pregel

import Graphs._
import cats.data.State

/**
 * Instances of this type class specify a particular Pregel algorithm.
 * In particular, the following information must be provided:
 *  - How to initialise an arbitrary graph
 *  - Initial message to be sent
 *  - Messages to be sent along a given edge
 *  - How to aggregate messages
 *  - How to update some vertex given some message
 *
 * But for the first item, it essentially follows the Pregel API for Spark.
 */
trait Program[G[_,_]]{

  // Type parameter that allow us, for instsance, to specify
  // input information for the implementation of the algorithm
  type P[_]

  /* TYPE CLASS DEPENDENCIES */

  /* Graph structure the algorithm will work with */
  val Graphs: Graphs[G]
  import Graphs._, graph.Id

  /* PRIMITIVE SIGNATURE */

  /**
   * To initialise an arbitrary graph we need two functions: one
   * to set up the initial value of each node, and another one to set
   * up the initial values of edges
   */
  case class Init[A,B](
    initV: (Id, A) => Vertex.Value,
    initE: (Id, Id, B) => Edge.Value
  )

  type Gen[_,_,_]
  def InitGen[A,B]: Gen[A,B,P[Init[A,B]]]

  // Initial message to be sent
  val initialMessage: P[Message.Value]

  // Message to be sent along a given edge
  def toBeSent(edge: graph.E): List[graph.M]

  // How to combine messages
  val S: cats.Semigroup[Message.Value]

  // How to update vertexes upon receiving a given message value
  def updateVertex(value: Message.Value): P[State[graph.V,Unit]]

  /* DERIVED */

  import cats.Monad, cats.data.NonEmptyList, cats.instances.list._
  import cats.syntax.flatMap._, cats.syntax.traverse._, cats.syntax.functor._

  // Just for illustration purposes. Particular interpreters will typically
  // bypass this implementation.
  def compute(g: G[Vertex.Value,Edge.Value],
    incoming: NonEmptyList[graph.M])(implicit M: Monad[P]): P[State[graph.V, List[graph.M]]] =
    updateVertex(incoming.map(Message.value).reduce(S)) map {
      _.inspect{ vertex => graph.outgoing(g,vertex) }
        .map(_.flatMap(toBeSent(_)))
    }

}

object Program{
  type WithGraphs[G[_,_],I <: Graphs[G]] = Program[G]{
    val Graphs: I
  }
}

