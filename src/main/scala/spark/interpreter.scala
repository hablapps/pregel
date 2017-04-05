package org.hablapps.pregel
package sparkPregel

import scala.reflect.ClassTag
import org.apache.spark.graphx.{Graph, VertexId, EdgeTriplet}

/**
 * Class of Spark interpreters for given labels of vertexes, edges
 * and messages.
 */
abstract class SparkInterpreter[VD: ClassTag, ED: ClassTag, A: ClassTag]
  extends Interpreter[Graph, cats.Id] with Serializable{

  /**
   * Constraint that forbids interpreting with this interpreter
   * Pregel programs that are not specified against Spark graphs.
   */
  val pregel: SparkProgram.Type[VD,ED,A]

  import pregel.Gen, pregel.P

  /**
   * The Spark interpreter does two things: first, it initialises
   * the input graph; second, it runs the Spark implementation of
   * pregel.
   */
  import cats.~>, cats.Id, cats.Functor, cats.syntax.functor._

  def run[V,E](gen: Gen[V,E,?]~>Id, input: P~>Id):
      Graph[V,E] => Graph[VD,ED] = g => {

    val init = input(gen(pregel.InitGen[V,E]))
    val pregelG = g.mapVertices(init.initV)
        .mapEdges(edge => init.initE(edge.srcId, edge.dstId, edge.attr))

    pregelG.pregel[A](
        input(pregel.initialMessage))(
        (id,v,a) => input(pregel.updateVertex(a)).runS((id,v)).value._2,
        e => pregel.toBeSent(e).toIterator,
        pregel.S.combine)
  }

}

object SparkInterpreter{

  def apply[VD: ClassTag, ED: ClassTag, A: ClassTag](
    program: SparkProgram.Type[VD,ED,A]) =
    new SparkInterpreter[VD,ED,A]{
      val pregel: program.type = program
    }

}