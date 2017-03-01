package org.hablapps.pregel

import org.apache.spark.graphx._
import scala.reflect.ClassTag

object ShortestPaths {
  /** Stores a map from the vertex id of a landmark to the distance to that landmark. */
  case class ShortestPath(length: Int, path: List[VertexId])
  type SPMap = Map[VertexId, ShortestPath]

  // private def makeMap(x: (VertexId, Int)*) = Map(x: _*)
  private def makeMap(x: (VertexId, ShortestPath)*) = Map(x: _*)

  // private def incrementMap(spmap: SPMap): SPMap = spmap.map { case (v, d) => v -> (d + 1) }
  private def incrementMap(dstId: VertexId, spmapDst: SPMap): SPMap = spmapDst map {
    case (vId, ShortestPath(length, path)) => (vId, ShortestPath(length+1, dstId :: path))
  }

  private def addMaps(spmap1: SPMap, spmap2: SPMap): SPMap =
    (spmap1.keySet ++ spmap2.keySet).map { k =>
      k ->
        ((spmap1.get(k), spmap2.get(k)) match {
          case (Some(s1), Some(s2)) => if (s1.length < s2.length) s1 else s2
          case (Some(s1), _) => s1
          case (_, Some(s2)) => s2
          case _ => throw new RuntimeException("Nonsense")
        })
    }.toMap

  /**
   * Computes shortest paths to the given set of landmark vertices.
   *
   * @tparam ED the edge attribute type (not used in the computation)
   *
   * @param graph the graph for which to compute the shortest paths
   * @param landmarks the list of landmark vertex ids. Shortest paths will be computed to each
   * landmark.
   *
   * @return a graph where each vertex attribute is a map containing the shortest-path distance to
   * each reachable landmark vertex.
   */
  def run[VD, ED: ClassTag](graph: Graph[VD, ED], landmarks: Seq[VertexId]): Graph[SPMap, ED] = {
    val spGraph = graph.mapVertices { (vid, attr) =>
      if (landmarks.contains(vid)) makeMap(vid -> ShortestPath(0, Nil)) else makeMap()
    }

    val initialMessage = makeMap()

    def vertexProgram(id: VertexId, attr: SPMap, msg: SPMap): SPMap = {
      addMaps(attr, msg)
    }

    def sendMessage(edge: EdgeTriplet[SPMap, _]): Iterator[(VertexId, SPMap)] = {
      val newAttr = incrementMap(edge.dstId, edge.dstAttr)
      if (edge.srcAttr != addMaps(newAttr, edge.srcAttr)) Iterator((edge.srcId, newAttr))
      else Iterator.empty
    }

    Pregel(spGraph, initialMessage)(vertexProgram, sendMessage, addMaps)
  }
}
