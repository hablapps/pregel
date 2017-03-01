name := "pregel"

scalaVersion := "2.11.8"

scalaOrganization := "org.typelevel"

scalaBinaryVersion := "2.11"

organization := "org.hablapps"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0",
  "org.scalaz" %% "scalaz-core" % "7.2.7",
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" %% "spark-sql" % "2.1.0",
  "org.apache.spark" %% "spark-graphx" % "2.1.0"
)

resourceDirectory in Compile := baseDirectory.value / "resources"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Ypartial-unification",
  // "-Xprint:typer",
  // "-Xlog-implicit-conversions",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:higherKinds")

initialCommands in console := """
  |import org.hablapps.gist._
  """.stripMargin
