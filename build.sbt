name := "rest"

version := "0.1"

scalaVersion := "2.13.1"

val http4sVersion = "0.21.0"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.slf4j" % "slf4j-simple" % "1.7.30"
)