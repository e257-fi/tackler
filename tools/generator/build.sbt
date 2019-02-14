import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "fi.e257",
      scalaVersion := "2.12.8",
      version      := "0.4.0"
    )),
    name := "generator",
    assemblyJarName in assembly := name.value + "-" + version.value + ".jar",
    libraryDependencies += scallop,
    libraryDependencies += betterFiles
  )
