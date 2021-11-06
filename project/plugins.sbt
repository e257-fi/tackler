// code generators
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.8.3")

// ScalaJS
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.7.1")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "1.1.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

// build & release
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

// QA tools
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.2")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.16")

// Publishing
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.10")

