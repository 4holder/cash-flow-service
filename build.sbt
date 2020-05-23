name := "cash-flow-service"
maintainer := "ronierison.silva@gmail.com"

version := "1.0"
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

lazy val UnitTestConf = config("unit") extend Test
lazy val IntegrationTestConf = config("integration") extend Test

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  guice,
  "com.typesafe.play" %% "play-json-joda" % "2.8.+",
  "com.pauldijou" %% "jwt-core" % "4.2.+",
  "com.typesafe.play" %% "play-slick" % "4.0.+",
  "org.postgresql" % "postgresql" % "42.2.+",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.+" % Test,
  "org.mockito" % "mockito-all" % "1.10.+" % Test
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

lazy val cashFlowService = (project in file("."))
  .enablePlugins(PlayScala)
  .configs(UnitTestConf, IntegrationTestConf)
  .settings(inConfig(UnitTestConf)(Defaults.testTasks): _*)
  .settings(inConfig(IntegrationTestConf)(Defaults.testTasks): _*)

lazy val unit = TaskKey[Unit]("unit", "Runs all unit tests.")
lazy val integration = TaskKey[Unit]("integration", "Runs all integration tests.")

coverageExcludedPackages := "<empty>;Reverse.*;Routes.*;RoutesPrefix.*;RunMigrations.*"

unit := (test in UnitTestConf).value
integration := (test in IntegrationTestConf).value

testOptions in UnitTestConf := Seq(Tests.Filter(testPackageName => testPackageName.startsWith("unit")))
javaOptions in UnitTestConf += s"-Dconfig.file=${baseDirectory.value}/conf/application-local.conf"

testOptions in IntegrationTestConf := Seq(Tests.Filter(testPackageName => testPackageName.startsWith("integration")))
javaOptions in IntegrationTestConf += s"-Dconfig.file=${baseDirectory.value}/conf/application-local.conf"

lazy val runDbMigrations = taskKey[Unit]("Run database migrations.")
lazy val seedDatabase = taskKey[Unit]("Seed database with some random values.")

fullRunTask(runDbMigrations, Compile, "infrastructure.tasks.RunMigrations")
fullRunTask(seedDatabase, Compile, "infrastructure.tasks.SeedDatabase")
