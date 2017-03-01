package org.hablapps.pregel

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SetUpData extends App {

  // 1. Set up Spark
  val conf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("frameless example")

  val spark = SparkSession.builder()
    .config(conf)
    .appName("SAMPLE")
    .getOrCreate()

  // implicit val sqlContext = spark.sqlContext
  spark.sparkContext.setLogLevel("WARN")

  import spark.implicits._

  // 2. Read from parquet file the vertices
  // ONLY TO SETUP
  val clients: List[Client] = List(
    Client(1, "John Doe", 27), Client(2, "John Doe", 27), Client(3, "John Doe", 27),
    Client(4, "John Doe", 27), Client(5, "John Doe", 27), Client(6, "John Doe", 27))
  spark.createDataset(clients).write.parquet("data/clients")

  // 3. Read from parquet file the edges
  // ONLY TO SETUP
  val transfers: List[Transfer] = List(
    Transfer(1, 4), Transfer(4, 1),
    Transfer(2, 4), Transfer(4, 2),
    Transfer(3, 4), Transfer(4, 3),
    Transfer(5, 4), Transfer(4, 5),
    Transfer(6, 4), Transfer(4, 6))
  spark.createDataset(transfers).write.parquet("data/transfers")

  spark.stop()

}
