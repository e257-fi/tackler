// code generators
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.8.3")

// ScalaJS
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.13.0")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "1.2.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.2.0")

// build & release
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

// QA tools
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.0.9")

// Publishing
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.17")

