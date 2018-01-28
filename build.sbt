name := "leaning-akka-stream"

version := "0.1"

scalaVersion := "2.12.3"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.4",
  "io.spray" %% "spray-json" % "1.3.3"
)

/** Wartremover configuration
  * http://www.wartremover.org/doc/install-setup.html
  *
  *
  *
  *
  */
wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing, Wart.Serializable)

wartremoverWarnings += Wart.Nothing

wartremoverWarnings ++= Seq(Wart.Any, Wart.Serializable)


/** Scala formatter configuration
  * https://github.com/lucidsoftware/neo-sbt-scalafmt
  *
  * > scalafmt       # format compile sources
    > test:scalafmt  # format test sources
    > sbt:scalafmt   # format .sbt source
  *
  * > scalafmt::test      # check compile sources
    > test:scalafmt::test # check test sources
    > sbt:scalafmt::test  # check .sbt sources
  * */
scalafmtOnCompile in ThisBuild := true