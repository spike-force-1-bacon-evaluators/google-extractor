logLevel := Level.Warn

// Generate coverage reports
//   sbt clean coverage test
//   sbt coverageReport
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

// Run linter for src/main
//   sbt scalastyle
// or for src/test
//   sbt test:scalastyle
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

// Create assembly jar
//   sbt assembly
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
