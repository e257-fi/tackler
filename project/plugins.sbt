// code generators
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.8.3")

// ScalaJS
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.5.0")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

// build & release
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// QA tools
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.13")

// Publishing
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.5")

