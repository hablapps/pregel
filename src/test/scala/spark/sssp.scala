package org.hablapps
package pregel
package examples

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.graphx.{Graph, VertexId, EdgeTriplet}

import org.scalatest._

class SSSPExample extends FlatSpec with Matchers{

  /* Start Spark */

  val spark = SparkSession
    .builder
    .appName(s"${this.getClass.getSimpleName}")
    .config("spark.master", "local")
    .getOrCreate()

  /* Execute program */

  RunSPSS(spark.sparkContext)

  /* Stop Spark */

  spark.stop()
}

object RunSPSS{

  def toString[A,B](g: Graph[A,B]): String =
    g.vertices.collect.mkString("\n") + "\n" +
    "-------" + "\n" +
    g.edges.collect.mkString("\n")


  def apply(sc: SparkContext) = {

    /* Input graph */

    val inputGraph: Graph[Long, Double] =
      GraphGenerators.logNormalGraph(sc, numVertices = 3)
        .mapEdges(e => e.attr.toDouble)

    println(toString(inputGraph))

    /* Spark interpreter for SSSP programs */

    import sparkPregel._

    val sssp = SparkInterpreter(SingleShortestPath(SparkGraphs[Double,Double,Double]()))

    /* Output graph */


    val outputGraph = sssp.run[Long,Double](
      input((_,_,b) => b), input(0)).apply(inputGraph)

    println("solution")
    println(outputGraph.vertices.collect.mkString("\n"))
  }
}

