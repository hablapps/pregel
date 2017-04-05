package org.hablapps.pregel
import scala.language.existentials
import cats.data.State

/**
 * This type class provides the essential graph structure to implement
 * Pregel algorithms.
 */
trait Graphs[G[_,_]]{
  import Graphs._

  val graph: Graph[G]
  val Edge: Edge[graph.E,graph.V]
  val Vertex: Vertex[graph.V,graph.Id]
  val Message: Message[graph.M,graph.Id]
}

object Graphs{

  // Alias used to constraint the labels of vertexes, edges and messages
  // in particular programs.

  type WithValues[G[_,_], VertexValue, EdgeValue, MessageValue] = Graphs[G]{
    val Edge: Edge[graph.E,graph.V]{ type Value = EdgeValue }
    val Vertex: Vertex[graph.V,graph.Id]{ type Value = VertexValue }
    val Message: Message[graph.M,graph.Id]{ type Value = MessageValue }
  }

  /* GRAPHS */

  trait Graph[G[_,_]]{
    type Id
    type V
    type E
    type M

    // Observers
    def numVertices[A,B](p: G[A,B]): Int
    def outgoing[A,B](p: G[A,B], vertex: V): List[E]
  }

  /* EDGES */

  trait Edge[E,V]{
    type Value
    // Observers
    def value(edge: E): Value
    def source(edge: E): V
    def destination(edge: E): V
  }

  /* VERTEXES */

  trait Vertex[V,Id]{
    type Value

    // Observers
    def id(vertex: V): Id
    def value(vertex: V): Value

    // Transformers
    def setValue(value: Value): State[V,Unit]
  }

  /* MESSAGES */

  trait Message[M,Id]{
    type Value

    // Constructor
    def create(value: Value, id: Id): M

    // Observers
    def value(m: M): Value
    def destination(m: M): Id
  }

}
