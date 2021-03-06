lazy val commonSettings = Seq(
  organization := "org.opensirf",
  version := "1.0.0"
)

lazy val root = (project in file(".")).
  enablePlugins(WarPlugin).
  settings(commonSettings: _*).
  settings(
    name := "OpenSIRF Server",
        artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        artifact.name + "-" + version.value + "." + artifact.extension
        },
    version := "1.0.0",
    crossTarget := new java.io.File("target"),
    webappWebInfClasses := true,
    packageOptions in sbt.Keys.`package` ++=
        (packageOptions in (Compile, packageBin)).value filter {
        case x: Package.ManifestAttributes => true
        case x => false
    }
)

crossPaths := false
publishTo := Some(Resolver.url("Artifactory Realm", new URL("http://200.144.189.109:58082/artifactory"))(Resolver.ivyStylePatterns))
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishMavenStyle := false
isSnapshot := true

unmanagedClasspath in Test += baseDirectory.value / "WebContent" / "WEB-INF" / "classes"

fork in Test := true

publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false

libraryDependencies ++= Seq(
    "org.opensirf" % "opensirf-core" % "1.0.0" changing(),
    "org.opensirf" % "opensirf-java-client" % "1.0.0" changing(),
    "com.novocode" % "junit-interface" % "0.4" % "test",
    "com.jcraft" % "jsch" % "0.1.54",
    "com.google.code.gson" % "gson" % "2.6.2",
    "org.glassfish.jersey.core" % "jersey-server" % "2.25",
    "org.glassfish.jersey.ext" % "jersey-entity-filtering" % "2.25",
    "org.glassfish.jersey.media" % "jersey-media-moxy" % "2.25",
    "aopalliance" % "aopalliance" % "1.0",
    "org.ow2.asm" % "asm-debug-all" % "5.0.2",
    "org.bouncycastle" % "bcpkix-jdk15on" % "1.49",
    "org.slf4j" % "slf4j-api" % "1.7.2",
    "org.slf4j" % "slf4j-jdk14" % "1.7.22",
    "org.apache.jclouds" % "jclouds-core" % "1.9.3",
    "org.apache.jclouds" % "jclouds-blobstore" % "1.9.3",
    "org.apache.jclouds.driver" % "jclouds-slf4j" % "1.9.3",
    "commons-io" % "commons-io" % "2.4",
    "org.apache.jclouds.api" % "openstack-swift" % "1.9.3"
)

resolvers += Resolver.url("SIRF Artifactory", url("http://200.144.189.109:58082/artifactory"))(Resolver.ivyStylePatterns)

