name := "cash-flow-service"
maintainer := "ronierison.silva@gmail.com"

version := "1.0"
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

lazy val UnitTestConf = config("unit") extend Test

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.+" % Test
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

lazy val cashFlowService = (project in file("."))
  .enablePlugins(PlayScala)
  .configs(UnitTestConf)
  .settings(inConfig(UnitTestConf)(Defaults.testTasks): _*)

lazy val unit = TaskKey[Unit]("unit", "Runs all Unit Tests.")

unit := (test in UnitTestConf).value

testOptions in UnitTestConf := Seq(Tests.Filter(testPackageName => testPackageName.startsWith("unit")))
javaOptions in UnitTestConf += s"-Dconfig.file=${baseDirectory.value}/conf/application-local.conf"