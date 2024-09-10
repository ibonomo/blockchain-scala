name := "blockchain-scala"

version := "0.1"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.8.2",
  "org.json4s" %% "json4s-native" % "4.0.6",
  "org.json4s" %% "json4s-jackson" % "4.0.6",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.70",
  "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided",
  "org.eclipse.jetty" % "jetty-server" % "9.4.44.v20210927", // Jetty server
  "org.eclipse.jetty" % "jetty-servlet" % "9.4.44.v20210927", // Jetty servlet handler
  "ch.qos.logback" % "logback-classic" % "1.2.11", // Adiciona a implementação do SLF4J
  "org.scalaj" %% "scalaj-http" % "2.4.2",  // Adiciona Scalaj-HTTP
  "org.scalatra" %% "scalatra-scalatest" % "2.8.2" % Test  // Dependência para testes com Scalatra
)
