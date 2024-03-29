/*
 * Copyright 2016-2022 E257.FI
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
  val betterFilesVersion = "3.9.2"
  val catsVersion = "2.10.0"
  val circeVersion = "0.14.6"
  val circeOpticsVersion = "0.15.0"
  val scalaParCollectionVersion = "1.0.4"
  val scalatestVersion = "3.2.17"
  val scallopVersion = "4.1.0"
  val configVersion = "1.4.3"
  val dirsuiteVersion = "0.32.0"
  val jgitVersion = "6.7.0.202309050840-r"
  val logbackVersion = "1.4.11"
  val slf4jVersion = "2.0.9"

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

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scallop = "org.rogach" %% "scallop" % scallopVersion
  val dirsuite = "fi.e257" %% "dirsuite" % dirsuiteVersion

  /* lib: java */
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % jgitVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion
  val typesafeConfig = "com.typesafe" % "config" % configVersion

}
