organization := "com.example"

name := "Voting"

version := "0.1.0.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-netty-server" % "0.8.4",
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
   "net.databinder" %% "unfiltered-specs2" % "0.8.4" % "test"
)

resolvers ++= Seq(
  "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
)
