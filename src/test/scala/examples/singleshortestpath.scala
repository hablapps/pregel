package org.hablapps
package pregel
package examples

import cats.Semigroup, cats.data.State, State._
import cats.syntax.flatMap._

abstract class SingleShortestPath[G[_,_]] extends Program[G]{

  /**
   * This kind of algorithm operates over a graph whose nodes, edges
   * and messages are labelled with double values
   */
  val Graphs: pregel.Graphs.WithValues[G,Double,Double,Double]
  import Graphs._, graph.Id, Vertex.{value => valueV, _}, Edge.{value => valueE, _}

  /**
   * To implement the Pregel algorithm we need the source vertex
   * (a value of type Id)
   */
  type P[t] = Id => t

  /**
   * To initialise an arbitrary graph, we just need the function
   * that extracts the weight from edges (vertexes will be set to
   * 0 or Infinity regardless of the current value)
   */
  type Gen[A,B,t] = ((Id,Id,B) => Double) => t

  def InitGen[A,B]: Gen[A,B,Id => Init[A,B]] =
    initE => source => Init(
      (id,_) => if (id == source) 0.0 else Double.PositiveInfinity,
      initE)

  /**
   * Initial distance communicated to nodes (note that the source vertex
   * is not needed)
   */
  val initialMessage: Id => Message.Value = _ => Double.PositiveInfinity

  /**
   * How to combine distances
   */
  val S = new Semigroup[Double]{
    def combine(a: Double, b: Double) = a.min(b)
  }

  /**
   * If the vertex to be updated is the source node, then assign 0;
   * otherwise, check if the proposed distance is lower than the current
   * one
   */
  def updateVertex(sent: Double): Id => State[graph.V,Unit] = source =>
    inspect{ id(_ : graph.V) == source }.ifM(
      setValue(0),
      inspect(valueV) >>= { current => setValue(current.min(sent)) })

  /**
   * If the edge contributes potentially to a new minimum distance
   * for its destination, send a message
   */
  def toBeSent(edge: graph.E): List[graph.M] = {
    val sum = valueV(Edge.source(edge))+valueE(edge)
    if (sum < valueV(Edge.destination(edge)))
      List(Message.create(sum,id(Edge.destination(edge))))
    else List()
  }
}

object SingleShortestPath{

  def apply[G[_,_]](
    graphs: pregel.Graphs.WithValues[G,Double,Double,Double]) =
    new SingleShortestPath[G] with Serializable{
      val Graphs: graphs.type = graphs
    }
}