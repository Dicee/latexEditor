lazy val commonSettings = Seq(
    version := "1.0",
    scalaVersion := "2.11.7",
    initialize := {
	  val _ = initialize.value
	  if (sys.props("java.specification.version") != "1.8")
	    sys.error("Java 8 is required for this project.")
	},
	exportJars := true,
	javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
	scalacOptions += "-target:jvm-1.8",
	libraryDependencies += "junit" % "junit" % "4.11" % "test"
)

lazy val root = 
	(project in file("."))
		.settings(commonSettings: _*)
		.settings(name := "latexEditor")

