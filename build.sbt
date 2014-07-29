scalaVersion := "2.10.4"

com.twitter.scrooge.ScroogeSBT.newSettings

libraryDependencies ++= Seq(
  "com.twitter" % "algebird-core_2.10" % "0.6.0",
  "com.twitter" %% "finagle-core" % "6.17.0",
  "com.twitter" %% "finagle-thrift" % "6.17.0",
  "com.twitter" %% "scrooge-core" % "3.16.0",
  "junit" % "junit" % "4.10" % "test",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "org.scalatest" %% "scalatest" %"1.9.1" % "test"
)
