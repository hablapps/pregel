package org.hablapps.pregel

import Graphs._

/**
 * This type class allows us to apply a given pregel algorithm
 * to arbitrary graphs, provided that we can initialise it and
 * interpret their specifications.
 */
trait Interpreter[G[_,_],Q[_]]{

  val pregel: Program[G]

  import pregel.Gen, pregel.P, pregel.Graphs._
  import cats.~>

  def run[V,E](gen: Gen[V,E,?]~>Q, input: P~>Q):
    G[V,E] => Q[G[Vertex.Value,Edge.Value]]
}