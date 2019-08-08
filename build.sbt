/*
 * Copyright 2016-2019 E257.FI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import TacklerTests._
import Dependencies._

import sbtcrossproject.{crossProject, CrossType}

lazy val scala_12 = "2.12.8"
lazy val scala_13 = "2.13.0"
lazy val supportedScalaVersions = List(scala_12, scala_13)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val commonSettings = Seq(
  organization := "fi.e257",
  version := "0.32.0-SNAPSHOT",
  scalaVersion := scala_13,
  crossScalaVersions := supportedScalaVersions,
  compileOrder := CompileOrder.JavaThenScala,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-explaintypes",
    "-feature",
    "-unchecked",
    "-Xcheckinit",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard"
  ),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) =>
        Seq(
          //"-Xfatal-warnings",
          // These won't work with 2.13
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Yno-adapted-args",
          "-Ypartial-unification",
        )
      case Some((2, 13)) =>
        Seq(
          // WIP: Disable fatal-warnings for Scala 2.13 "-Xfatal-warnings",
          "-Ywarn-unused:imports",
	)
      case _ => Nil
    }
  },
  Compile / console / scalacOptions --= Seq(
    "-Ywarn-unused:imports",
    "-Xfatal-warnings"
  ),
  Compile / compile / wartremoverWarnings ++= Warts.allBut(
    Wart.ToString,
    Wart.NonUnitStatements,
    Wart.PublicInference,
    Wart.Throw //https://github.com/puffnfresh/wartremover/commit/869763999fcc1fd685c1a8038c974854457b608f
  ),
  publishTo := sonatypePublishTo.value,
)

/**
 * if "name" is defined in commonSettings, it will cause
  * circular dependencies with sub-projects
  */
lazy val tackler = (project in file(".")).
  aggregate(api.js, api.jvm, core, cli).
  dependsOn(api.js, api.jvm, core, cli).
  settings(noPublishSettings).
  settings(commonSettings: _*).
  settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    run / fork := true
  )

lazy val api = crossProject(JSPlatform, JVMPlatform).
  crossType(CrossType.Pure).in(file("api")).
  settings(commonSettings: _*).
  settings(
    name := "tackler-api",
    libraryDependencies += "io.circe" %%% "circe-core" % circeVersion,
    libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion,
    libraryDependencies += "io.circe" %%% "circe-parser" % circeVersion,
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVersion % Test
  ).
  jvmSettings(
  ).
  jsSettings(
    coverageEnabled := false,
    coverageExcludedPackages := ".*",
    Test / test := {}
)


lazy val core = (project in file("core")).
  dependsOn(api.jvm).
  enablePlugins(Antlr4Plugin).
  configs(IntegrationTest).
  settings(Defaults.itSettings).
  settings(commonSettings: _*).
  settings(
    name := "tackler-core",
    fork := true,
    antlr4Version in Antlr4 := "4.7.2",
    antlr4GenListener in Antlr4 := false,
    antlr4GenVisitor in Antlr4 := false,
    antlr4TreatWarningsAsErrors in Antlr4 := true,
    antlr4PackageName in Antlr4 := Some("fi.e257.tackler.parser")
  ).
  settings(
    libraryDependencies += betterFiles,
    libraryDependencies += cats_core,
    libraryDependencies ++= circe_deps,
    libraryDependencies ++= circe_deps_test,
    libraryDependencies += typesafeConfig,
    libraryDependencies += jgit,
    libraryDependencies += scalaCollCompat,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0")
        case _ => Nil
      }
    },
    libraryDependencies += scalatest % "it,test",
  )

val gitCommitId = SettingKey[String]("gitCommit")
val gitLocalChanges = SettingKey[Boolean]("gitUncommittedChanges")

lazy val cli = (project in file("cli")).
  enablePlugins(BuildInfoPlugin).
  dependsOn(core).
  configs(IntegrationTest).
  settings(Defaults.itSettings).
  settings(noPublishSettings).
  settings(commonSettings: _*).
  settings(
    fork := true,
    Test / baseDirectory := file((Test / baseDirectory).value + "/.."),
    IntegrationTest / baseDirectory := (Test / baseDirectory).value,
    IntegrationTest / testOptions += {
      // The evaluation of `streams` inside an anonymous function is prohibited.
      // https://github.com/sbt/sbt/issues/3266
      // https://github.com/jeffwilde/sbt-dynamodb/commit/109ea03837b1c1b4f45723c200d7aa5c34bb6e8b
      val log = sLog.value
      Tests.Setup(() => TacklerTests.setup("tests", log))
    },
    assembly / test := {},
    assembly / assemblyJarName := "tackler-cli" + "-" + version.value + ".jar",
    assembly / assemblyMergeStrategy := {
      // Fix module-info.class with JDK 8 vs. JDK 11,
      // it's not needed, this is app, not lib
      case "module-info.class" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    gitCommitId := git.gitHeadCommit.value.getOrElse("Not available"),
    gitLocalChanges := git.gitUncommittedChanges.value,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, gitCommitId, gitLocalChanges),
    // Do not enable BuildInfoOption.BuildTime, it breaks coverage analysis of test + it:test
    // targets because it causes recompilation, which in turn causes scoverage to clear instrumentation cache
    // https://github.com/scoverage/sbt-scoverage/issues/277
    // BuildInfo.BuildTime is also more hashle than what it is worth -> remove it altogether
    buildInfoPackage := "fi.e257.tackler.cli",
    buildInfoUsePackageAsPath := true,
    buildInfoObject := "BuildInfo"
  ).
  settings(
    libraryDependencies += betterFiles,
    libraryDependencies += logback,
    libraryDependencies += scallop,
    libraryDependencies += typesafeConfig,
    libraryDependencies += logback,
    libraryDependencies += scalaCollCompat,
    libraryDependencies += scalatest % "it,test",
    libraryDependencies += dirsuite % "it,test"
  )

