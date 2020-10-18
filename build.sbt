name := "tinto-de-verano"
version := "0.0.1-alpha"

description := "playground with sangria"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.4.2",
  "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1",

  "com.typesafe.akka" %% "akka-http" % "10.1.10",
  "de.heikoseeberger" %% "akka-http-circe" % "1.29.1",

  "io.circe" %% "circe-core" % "0.12.1",
  "io.circe" %% "circe-parser" % "0.12.1",
  "io.circe" %% "circe-optics" % "0.9.3",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

