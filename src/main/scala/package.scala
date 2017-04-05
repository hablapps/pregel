package org.hablapps
package object pregel{

  // Auxiliary functions

  import cats.{~>, Id}

  def input[S](s: S) = λ[(S => ?) ~> Id]{ _(s) }

}