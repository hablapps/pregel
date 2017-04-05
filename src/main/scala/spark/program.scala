package org.hablapps.pregel
package sparkPregel

import org.apache.spark.graphx.{Graph}

/**
 * Type alias to specify the kind of programs that can be run by
 * the Spark interpreter
 */
object SparkProgram{

  type Type[VD,ED,A] = Program.WithGraphs[Graph,SparkGraphs[VD,ED,A]]

}