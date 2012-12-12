sbtPlugin := true

name := "sbt-jasmine-plugin"

organization := "com.gu"

version := "0.8-SNAPSHOT"

libraryDependencies += "org.mozilla" % "rhino" % "1.7R3"

// don't bother publishing javadoc
publishArtifact in (Compile, packageDoc) := false

publishTo <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
        )
    )
}
