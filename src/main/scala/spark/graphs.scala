package org.hablapps.pregel
package sparkPregel

import org.apache.spark.graphx.{Graph => SGraph, VertexId, EdgeTriplet}

/**
 * Pregel-like graph structure for Spark graphs.
 */
case class SparkGraphs[VD,ED,A]() extends Graphs[SGraph] with Serializable{
  import Graphs._

  val graph = new Graph[SGraph] with Serializable{
    type Id = VertexId
    type V = (VertexId,VD)
    type E = EdgeTriplet[VD,ED]
    type M = (VertexId,A)

    // Observers: TBD
    def numVertices[A,B](p: SGraph[A,B]): Int = ???
    def outgoing[A,B](p: SGraph[A,B], vertex: (VertexId,VD)): List[E] = ???
  }

  val Edge = new Edge[EdgeTriplet[VD,ED],(VertexId,VD)] with Serializable{
    type Value = ED

    // Observers
    def value(edge: EdgeTriplet[VD,ED]): ED =
      edge.attr
    def source(edge: EdgeTriplet[VD,ED]): (VertexId,VD) =
      (edge.srcId, edge.srcAttr)
    def destination(edge: EdgeTriplet[VD,ED]): (VertexId,VD) =
      (edge.dstId, edge.dstAttr)
  }

  val Vertex = new Vertex[(VertexId,VD),VertexId] with Serializable{
    type Value = VD

    // Observers
    def id(vertex: (VertexId,VD)): VertexId = vertex._1
    def value(vertex: (VertexId,VD)): VD = vertex._2

    // Transformers
    import cats.data.State

    def setValue(value: VD): State[(VertexId,VD),Unit] =
      State.modify{ case (id,_) => (id,value) }
  }

  val Message = new Message[(VertexId,A),VertexId] with Serializable{
    type Value = A

    // Constructors
    def create(value: A, id: VertexId): (VertexId,A) =
      (id, value)

    // Observers
    def value(m: (VertexId,A)): A = m._2
    def destination(m: (VertexId,A)): VertexId = m._1
  }

}