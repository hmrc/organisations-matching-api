import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Keys.unmanagedSourceDirectories
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "organisations-matching-api"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(publishingSettings: _*)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies(),
    playDefaultPort := 9654,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it", base / "test-common")).value,
    unmanagedSourceDirectories in Test := (baseDirectory in Test) (base => Seq(base / "test", base / "test-common")).value,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
  )
  .settings(resolvers += Resolver.jcenterRepo)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map {
    test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }
