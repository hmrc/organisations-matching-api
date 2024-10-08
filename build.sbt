import play.sbt.routes.RoutesKeys

val appName = "organisations-matching-api"

lazy val ItTest = config("it") extend Test

lazy val ComponentTest = config("component") extend Test

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"

TwirlKeys.templateImports := Seq.empty
RoutesKeys.routesImport := Seq("uk.gov.hmrc.organisationsmatchingapi.Binders._")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    onLoadMessage := "",
    majorVersion := 0,
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    scalacOptions += "-Wconf:src=routes/.*:s",
    Test / testOptions := Seq(Tests.Filter(unitFilter))
  )
  .configs(ItTest)
  .settings(inConfig(ItTest)(Defaults.testSettings) *)
  .settings(
    ItTest / Keys.fork := true,
    ItTest / unmanagedSourceDirectories := (ItTest / baseDirectory)(base => Seq(base / "test")).value,
    ItTest / testOptions := Seq(Tests.Filter(intTestFilter))
  )
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings) *)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(componentFilter)),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value,
    ComponentTest / parallelExecution := false
  )
  .settings(CodeCoverageSettings.settings *)
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(PlayKeys.playDefaultPort := 9657)
