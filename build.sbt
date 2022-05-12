lazy val V = _root_.scalafix.sbt.BuildInfo

lazy val rulesCrossVersions = Seq(V.scala213, V.scala212)
lazy val scala3Version = "3.1.3-RC3"

def isScala2(v: Option[(Long, Long)]): Boolean = v.exists(_._1 == 2)
def isScala3(v: Option[(Long, Long)]): Boolean = v.exists(_._1 == 3)

def crossSetting[A](
    scalaVersion: String,
    if2: List[A] = Nil,
    if3: List[A] = Nil
): List[A] =
  CrossVersion.partialVersion(scalaVersion) match {
    case partialVersion if isScala2(partialVersion) => if2
    case partialVersion if isScala3(partialVersion) => if3
    case _ => Nil
  }


val inputSettings = List(
  scalacOptions ++= crossSetting(
    scalaVersion.value,
    if2 = List(
      "-P:semanticdb:synthetics:on"
    ),
    if3 = List(
    )
  ),
)


inThisBuild(
  List(
    name := "scalafix-unused",
    organization := "com.github.tanishiking",
    homepage := Some(url("https://github.com/tanishiking/scalafix-unused")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "tanishiking",
        "Rikito Taniguchi",
        "rikiriki1238@gmail.com",
        url("https://github.com/tanishiking")
      )
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val `scalafix-unused` = (project in file("."))
  .aggregate(
    rules.projectRefs ++
      input.projectRefs ++
      output.projectRefs ++
      tests.projectRefs: _*
  )
  .settings(
    publish / skip := true
  )

lazy val rules = projectMatrix
  .settings(
    moduleName := "scalafix-unused",
    scalacOptions += "-P:semanticdb:synthetics:on",
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(rulesCrossVersions)

lazy val input = projectMatrix
  .settings(
    inputSettings,
    publish / skip := true
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = rulesCrossVersions :+ scala3Version)
  .disablePlugins(ScalafixPlugin)

lazy val output = projectMatrix
  .settings(
    inputSettings,
    publish / skip := true
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = rulesCrossVersions :+ scala3Version)

lazy val testsAggregate = Project("tests", file("target/testsAggregate"))
  .aggregate(tests.projectRefs: _*)

lazy val tests = projectMatrix
  .settings(
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories :=
      TargetAxis
        .resolve(output, Compile / unmanagedSourceDirectories)
        .value,
    scalafixTestkitInputSourceDirectories :=
      TargetAxis
        .resolve(input, Compile / unmanagedSourceDirectories)
        .value,
    scalafixTestkitInputClasspath :=
      TargetAxis.resolve(input, Compile / fullClasspath).value,
    scalafixTestkitInputScalacOptions :=
      TargetAxis.resolve(input, Compile / scalacOptions).value,
    scalafixTestkitInputScalaVersion :=
      TargetAxis.resolve(input, Compile / scalaVersion).value
  )
  .defaultAxes(
    rulesCrossVersions.map(VirtualAxis.scalaABIVersion) :+ VirtualAxis.jvm: _*
  )
  .customRow(
    scalaVersions = Seq(V.scala212),
    axisValues = Seq(TargetAxis(scala3Version), VirtualAxis.jvm),
    settings = Seq()
  )
  .customRow(
    scalaVersions = Seq(V.scala213),
    axisValues = Seq(TargetAxis(V.scala213), VirtualAxis.jvm),
    settings = Seq(
      scalacOptions += "-P:semanticdb:synthetics:on",
    )
  )
  .customRow(
    scalaVersions = Seq(V.scala212),
    axisValues = Seq(TargetAxis(V.scala212), VirtualAxis.jvm),
    settings = Seq(
      scalacOptions += "-P:semanticdb:synthetics:on",
    )
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)


lazy val benchmarks = projectMatrix
  .settings(
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories :=
      TargetAxis
        .resolve(output, Compile / unmanagedSourceDirectories)
        .value,
    scalafixTestkitInputSourceDirectories :=
      TargetAxis
        .resolve(input, Compile / unmanagedSourceDirectories)
        .value,
    scalafixTestkitInputClasspath :=
      TargetAxis.resolve(input, Compile / fullClasspath).value,
    scalafixTestkitInputScalacOptions :=
      TargetAxis.resolve(input, Compile / scalacOptions).value,
    scalafixTestkitInputScalaVersion :=
      TargetAxis.resolve(input, Compile / scalaVersion).value,

    Jmh / sourceDirectory := (Test / sourceDirectory).value,
    Jmh / classDirectory := (Test / classDirectory).value,
    Jmh / dependencyClasspath := (Test / dependencyClasspath).value,
    // rewire tasks, so that 'jmh:run' automatically invokes 'jmh:compile' (otherwise a clean 'jmh:run' would fail)
    Jmh / compile := (Jmh / compile).dependsOn(Test / compile).value,
    Jmh / run := (Jmh / run).dependsOn(Jmh / Keys.compile).evaluated,
  )
  .defaultAxes(
    rulesCrossVersions.map(VirtualAxis.scalaABIVersion) :+ VirtualAxis.jvm: _*
  )
  .customRow(
    scalaVersions = Seq(V.scala213),
    axisValues = Seq(TargetAxis(V.scala213), VirtualAxis.jvm),
    settings = Seq(
      scalacOptions += "-P:semanticdb:synthetics:on",
    )
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
  .enablePlugins(JmhPlugin)
