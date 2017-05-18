import Settings._


val `microservices-discovery` = project
  .in(file("."))
  .aggregate(aggregatedProjects: _*)

lazy val `customer-service` = project
  .enablePlugins(DeployApp, DockerPlugin)
  .dependsOn(`common`)
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.Service
  )

lazy val `account-service` = project
  .enablePlugins(DeployApp, DockerPlugin)
  .dependsOn(`common`)
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.Service
  )

lazy val `customer-ui` = project
  .enablePlugins(DeployApp, DockerPlugin)
  .dependsOn(`customer-service`, `account-service`, `common`)
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.Service
  )



lazy val `common` = project
  .enablePlugins(DeployApp, DockerPlugin)
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.Service
  )


lazy val `locationservice` = project
  .enablePlugins(DeployApp, DockerPlugin)
  .dependsOn(`common`)
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.Service
  )


lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `account-service`,
  `customer-service`,
  `customer-ui`
)
