ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.pll"

lazy val motorolaradio = (project in file ("."))
  .settings(
    name := "MotorolaRadio",
    libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.7",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.19" ,
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.21",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.1.7",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
    libraryDependencies += "com.h2database" % "h2" % "1.4.199" % Test,
    libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.0",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test,
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
)
