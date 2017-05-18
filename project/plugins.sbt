addSbtPlugin("org.scalastyle"                    %% "scalastyle-sbt-plugin"  % "0.8.0")
addSbtPlugin("com.geirsson"                      % "sbt-scalafmt"            % "0.6.1")
addSbtPlugin("com.dwijnand"                      % "sbt-dynver"              % "1.1.0")
addSbtPlugin("com.lightbend.paradox"             % "sbt-paradox"             % "0.2.8")
addSbtPlugin("com.eed3si9n"                      % "sbt-unidoc"              % "0.4.0")
addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings"        % "1.0.0")
addSbtPlugin("com.timushev.sbt"                  % "sbt-updates"             % "0.3.0")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-ghpages"             % "0.6.0")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-site"                % "1.2.0")
addSbtPlugin("org.scoverage"                     % "sbt-scoverage"           % "1.5.0")
addSbtPlugin("org.scoverage"                     % "sbt-coveralls"           % "1.1.0")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-native-packager"     % "1.2.0-M8")
addSbtPlugin("com.github.tototoshi"              % "sbt-build-files-watcher" % "0.1.1")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-multi-jvm"           % "0.3.8")
addSbtPlugin("io.spray"                          % "sbt-revolver"            % "0.8.0")
addSbtPlugin("com.dwijnand"                      % "sbt-project-graph"       % "0.2.0")
addSbtPlugin("net.virtual-void"                  % "sbt-dependency-graph"    % "0.8.2")
addSbtPlugin("com.eed3si9n"                      % "sbt-buildinfo"           % "0.7.0")
//addSbtPlugin("com.lightbend.conductr" % "sbt-conductr" % "2.3.4")

resolvers += Resolver.url("2m-sbt-plugin-releases",
  url("https://dl.bintray.com/2m/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
resolvers += Resolver.bintrayRepo("twtmt", "maven")

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  //"-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture"
)
