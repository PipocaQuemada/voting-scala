organization := "com.example"

name := "Voting"

version := "0.1.0.0"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-netty-server" % "0.8.4",
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
   "net.databinder" %% "unfiltered-specs2" % "0.8.4" % "test",
   "com.websudos" %% "phantom-dsl" % "1.5.4",
   "com.websudos" %% "phantom-zookeeper" % "1.5.4",
   "com.twitter" %% "finagle-zookeeper" % "6.24.0"
)


resolvers ++= Seq(
  "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/",
  "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Websudos releases"                at "http://maven.websudos.co.uk/ext-release-local",
  "mvn-repo"                         at "http://mvnrepository.com",
  "Twitter Repository"               at "http://maven.twttr.com"
  )

