import play.core.PlayVersion
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val hmrcMongo = "uk.gov.hmrc.mongo"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrcMongo             %% "hmrc-mongo-play-27"         % "0.47.0",
    hmrc                  %% "bootstrap-backend-play-27"  % "4.1.0",
    hmrc                  %% "play-hmrc-api"              % "6.2.0-play-27"
  )

  def test(scope: String = "test, it") = Seq(
    hmrc                      %% "bootstrap-test-play-27"   % "4.1.0"               % scope,
    hmrcMongo                 %% "hmrc-mongo-test-play-27"  % "0.47.0"              % scope,
    "org.scalatest"           %% "scalatest"                % "3.2.5"               % scope,
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current   % scope,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.62.2"              % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"               % scope,
    "org.pegdown"             %  "pegdown"                  % "1.6.0"               % scope,
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.27.0"              % scope,
    "org.mockito"             %  "mockito-core"             % "3.8.0"               % scope
  )

}
