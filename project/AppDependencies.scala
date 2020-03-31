import play.core.PlayVersion
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc                  %% "simple-reactivemongo"       % "7.23.0-play-26",
    hmrc                  %% "bootstrap-play-26"          % "1.3.0",
    hmrc                  %% "play-hmrc-api"              % "3.9.0-play-26",
    "com.eclipsesource"   %% "play-json-schema-validator" % "0.9.4",
    "org.typelevel"       %% "cats-core"                  % "2.0.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = testCommon(scope)
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val scope: String = "it"

      override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
        "com.github.tomakehurst" % "wiremock-jre8" % "2.22.0" % scope
      )
    }.test
  }

  private def testCommon(scope: String) = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % scope,
    "com.typesafe.play"       %% "play-test"                % current                 % scope,
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % scope,
    "org.mockito"             %  "mockito-core"             % "2.15.0"                % scope
  )

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()

}
