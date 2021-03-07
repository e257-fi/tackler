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
import sbt._
import Keys._

object Dependencies {
  /*
   * Versions
   */
  val betterFilesVersion = "3.9.1"
  val catsVersion = "2.3.1"
  val circeVersion = "0.13.0"
  val circeOpticsVersion = "0.13.0"
  val scalaParCollectionVersion = "1.0.0"
  val scalatestVersion = "3.2.5"
  val scallopVersion = "4.0.2"
  val scalaCollCompatVersion = "2.4.2"
  val configVersion = "1.4.1"
  val dirsuiteVersion = "0.31.0"
  val jgitVersion = "5.10.0.202012080955-r"
  val logbackVersion = "1.2.3"
  val slf4jVersion = "1.7.30"

  /*
   * Libraries
   */
  /* lib: scala */
  val betterFiles = "com.github.pathikrit" %% "better-files" % betterFilesVersion
  val cats_core = "org.typelevel" %% "cats-core" % catsVersion
  val cats_kernel = "org.typelevel" %% "cats-kernel" % catsVersion
  val circe_deps = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
  ).map(_ % circeVersion)

  val circe_deps_test = Seq(
    "io.circe" %% "circe-optics"
  ).map(_ % circeOpticsVersion % "it,test")

  val scalaParCollection = "org.scala-lang.modules" %% "scala-parallel-collections" % scalaParCollectionVersion
  val scalaCollCompat = "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollCompatVersion

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scallop = "org.rogach" %% "scallop" % scallopVersion
  val dirsuite = "fi.e257.testing" %% "dirsuite" % dirsuiteVersion

  /* lib: java */
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % jgitVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion
  val typesafeConfig = "com.typesafe" % "config" % configVersion

}
