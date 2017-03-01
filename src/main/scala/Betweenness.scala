package org.hablapps.pregel

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.graphx._
import frameless.TypedDataset
import frameless.syntax._

object Betweenness extends App {

  // 1. Set up Spark
  val conf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("frameless example")

  val spark = SparkSession.builder()
    .config(conf)
    .appName("SAMPLE")
    .getOrCreate()

  implicit val sqlContext = spark.sqlContext
  spark.sparkContext.setLogLevel("WARN")

  import spark.implicits._

  // 2. Read from parquet file the vertices
  val clientsDS: TypedDataset[Client] =
    spark.read.parquet("data/clients").as[Client].typed
  val clientsRDD: RDD[(VertexId, String)] = clientsDS.select(
    clientsDS('id).cast[VertexId],
    clientsDS('name)).rdd

  // 3. Read from parquet file the edges
  val transfersDS = spark.read.parquet("data/transfers").as[Transfer].typed
  val transfersRDD: RDD[Edge[Unit]] = transfersDS.select(
    transfersDS('orig).cast[VertexId],
    transfersDS('dest).cast[VertexId]).rdd
    .map { case (orig, dest) => Edge(orig, dest, ()) }

  // 4. Create the Graph
  val myGraph: Graph[String, Unit] = Graph(clientsRDD, transfersRDD)

  // 5. Calculate shortest paths
  import ShortestPaths.ShortestPath
  val spGraph = ShortestPaths
    .run(myGraph, myGraph.vertices.map(_._1).collect())
    .mapVertices {
      case (vId, smap) => (smap mapValues {
        case ShortestPath(l, p) => ShortestPath(l, vId :: p)
      }).map(identity)
    }

  // 6. Calculate betweenness
  val shortestPaths: RDD[List[VertexId]] =
    spGraph.vertices.flatMap(_._2.toList.map(_._2.path))

  val betweenness: List[(VertexId, Double)] =
    shortestPaths.aggregate(Map.empty[VertexId, Long])({ (acc, path) =>
      path.foldLeft(acc) { (acc2, node) =>
        acc2 + (node -> (1L + acc2.getOrElse(node, 0L)))
      }
    }, { (acc1, acc2) =>
      acc1.foldLeft(acc2) { case (acc3, (k, v)) =>
        acc3 + (k -> (v + acc3.getOrElse(k, 0L)))
      }
    })
    .mapValues(_.toDouble / shortestPaths.count)
    .toList.sortBy(_._2).reverse

  // 7. Print results
  println(s"""
    |RESULT:
    |
    |${shortestPaths.collect().mkString("\n")}
    |
    |${betweenness.mkString("\n")}
    |""".stripMargin)

  spark.stop()

}
