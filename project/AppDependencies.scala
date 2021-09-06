import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val hmrcMongo = "uk.gov.hmrc.mongo"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrcMongo             %% "hmrc-mongo-play-27"         % "0.53.0",
    hmrc                  %% "bootstrap-backend-play-28"  % "5.12.0",
    hmrc                  %% "play-hmrc-api"              % "6.4.0-play-28",
    hmrc                  %% "play-hal"                   % "2.1.0-play-27",
    hmrc                  %% "mongo-caching"              % "7.0.0-play-28",
    hmrc                  %% "json-encryption"            % "4.10.0-play-28"
  )

  def test(scope: String = "test, it, component") = Seq(
    hmrc                           %% "bootstrap-test-play-28"   % "5.12.0"                 % scope,
    hmrcMongo                      %% "hmrc-mongo-test-play-27"  % "0.53.0"                % scope,
    "org.scalatestplus"            %% "mockito-3-4"              % "3.2.7.0"               % scope,
    "org.scalatest"                %% "scalatest"                % "3.2.9"                 % scope,
    "com.typesafe.play"            %% "play-test"                % PlayVersion.current     % scope,
    "com.vladsch.flexmark"         %  "flexmark-all"             % "0.36.8"                % scope,
    "org.scalatestplus.play"       %% "scalatestplus-play"       % "4.0.3"                 % scope,
    "org.pegdown"                  %  "pegdown"                  % "1.6.0"                 % scope,
    "com.github.tomakehurst"       %  "wiremock-jre8"            % "2.27.2"                % scope,
    "org.mockito"                  %  "mockito-core"             % "3.8.0"                 % scope,
    hmrc                           %% "service-integration-test" % "1.1.0-play-27"         % scope,
    "org.scalaj"                   %% "scalaj-http"              % "2.4.2"                 % scope,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"     % "2.12.2"                % scope
  )

}