/*
 * Copyright 2016-2021 E257.FI
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

lazy val scala_12 = "2.12.13"
lazy val scala_13 = "2.13.5"

ThisBuild / organization := "fi.e257"
ThisBuild / version := "0.36.0-SNAPSHOT"
ThisBuild / scalaVersion := scala_13
ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val supportedScalaVersions = List(scala_12, scala_13)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val commonSettings = Seq(
  crossScalaVersions := supportedScalaVersions,
  compileOrder := CompileOrder.JavaThenScala,
  scalacOptions ++= Seq(
    // This is an adapted list of Rob Norris (tpolecat) settings: 
    // https://tpolecat.github.io/2017/04/25/scalac-flags.html
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    //"-Xfatal-warnings",  // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
    "-Ywarn-unused:params", // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates", // Warn if a private member is unused.
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
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
  )
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
    libraryDependencies += "org.typelevel" %%% "cats-kernel" % catsVersion,
    libraryDependencies += "org.typelevel" %%% "cats-core" % catsVersion,
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
    libraryDependencies += cats_kernel,
    libraryDependencies ++= circe_deps,
    libraryDependencies ++= circe_deps_test,
    libraryDependencies += typesafeConfig,
    libraryDependencies += jgit,
    libraryDependencies += slf4j,
    libraryDependencies += scalaCollCompat,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          Seq(scalaParCollection)
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
      // Scala-collection-compat vs. scala 2.12.13: both have support for nowarn,
      // see: https://github.com/scala/scala-collection-compat/issues/426
      case PathList("scala", "annotation", "nowarn.class" | "nowarn$.class") =>
          MergeStrategy.first
      // Fix (e.g. discard) module-info.class with JDK 8 vs. JDK 11,
      // it's not needed, this is an app, not lib
      // Bouncycastle files are living nowdays under META-INF/versions/9, hence endsWith
      case x if x.endsWith("/module-info.class") => MergeStrategy.discard
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
    libraryDependencies ++= circe_deps,
    libraryDependencies += logback,
    libraryDependencies += slf4j,
    libraryDependencies += scallop,
    libraryDependencies += typesafeConfig,
    libraryDependencies += scalaCollCompat,
    libraryDependencies += scalatest % "it,test",
    libraryDependencies += dirsuite % "it,test"
  )

