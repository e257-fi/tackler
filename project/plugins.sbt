// code generators
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.8.2")

// ScalaJS
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.28")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "0.6.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")

// build & release
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// QA tools
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.2")

// Publishing
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

