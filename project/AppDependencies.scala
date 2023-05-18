import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val hmrcMongo = "uk.gov.hmrc.mongo"
  val mongoVersion = "0.73.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrcMongo  %% "hmrc-mongo-play-28"         % mongoVersion,
    hmrc       %% "bootstrap-backend-play-28"  % "7.8.0",
    hmrc       %% "play-hmrc-api"              % "7.1.0-play-28",
    hmrc       %% "play-hal"                   % "3.4.0-play-28",
    hmrc       %% "json-encryption"            % "5.1.0-play-28"
  )

  def test(scope: String = "test, it, component") = Seq(
    hmrc                           %% "bootstrap-test-play-28"   % "7.8.0"             % scope,
    hmrcMongo                      %% "hmrc-mongo-test-play-28"  % "0.73.0"            % scope,
    "org.scalatestplus"            %% "mockito-3-4"              % "3.2.7.0"           % scope,
    "org.scalatestplus"            %% "scalacheck-1-15"          % "3.2.10.0"          % scope,
    "org.scalatest"                %% "scalatest"                % "3.2.9"             % scope,
    "com.typesafe.play"            %% "play-test"                % PlayVersion.current % scope,
    "com.vladsch.flexmark"         %  "flexmark-all"             % "0.36.8"            % scope,
    "org.scalatestplus.play"       %% "scalatestplus-play"       % "4.0.3"             % scope,
    "org.pegdown"                  %  "pegdown"                  % "1.6.0"             % scope,
    "com.github.tomakehurst"       %  "wiremock-jre8"            % "2.27.2"            % scope,
    "org.mockito"                  %  "mockito-core"             % "3.8.0"             % scope,
    hmrc                           %% "service-integration-test" % "1.3.0-play-28"     % scope,
    "org.scalaj"                   %% "scalaj-http"              % "2.4.2"             % scope,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"     % "2.12.2"            % scope
  )

}
