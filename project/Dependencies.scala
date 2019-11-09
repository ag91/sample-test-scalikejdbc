import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val scalikejdbc = "org.scalikejdbc" %% "scalikejdbc" % "3.4.0"
  lazy val scalikejdbcTest = "org.scalikejdbc" %% "scalikejdbc-test" % "3.4.0"
  lazy val h2database = "com.h2database" % "h2"  % "1.4.200"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
