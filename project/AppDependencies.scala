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
    hmrc                  %% "play-hmrc-api"              % "3.9.0-play-26"
  )

  def test(scope: String = "test, it") = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "4.1.0"               % scope,
    "org.scalatest"           %% "scalatest"                % "3.2.5"               % scope,
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current   % scope,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8"              % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"               % scope,
    "org.pegdown"             %  "pegdown"                  % "1.6.0"               % scope,
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.22.0"              % scope,
    "org.mockito"             %  "mockito-core"             % "3.8.0"               % scope
  )

}
