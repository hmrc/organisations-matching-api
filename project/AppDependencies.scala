import sbt.*

object AppDependencies {
  val hmrc = "uk.gov.hmrc"
  val playVersion = "play-30"
  val hmrcMongoVersion = "1.7.0"
  var bootstrapVersion = "8.4.0"

  val compile: Seq[ModuleID] = Seq(
    s"$hmrc.mongo" %% s"hmrc-mongo-$playVersion"    % hmrcMongoVersion,
    hmrc           %% s"play-hmrc-api-$playVersion" % "8.0.0",
    hmrc           %% s"play-hal-$playVersion"      % "4.0.0",
    hmrc           %% s"crypto-json-$playVersion"   % "7.6.0"
  )

  def test(scope: String = "test, it, component"): Seq[ModuleID] = Seq(
    hmrc                           %% s"bootstrap-test-$playVersion" % bootstrapVersion % scope,
    "org.scalatestplus"            %% "scalacheck-1-17"          % "3.2.16.0"          % scope,
    "org.scalaj"                   %% "scalaj-http"              % "2.4.2"             % scope
  )
}
