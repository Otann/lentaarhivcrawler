name := "lenta-bio-export"

version := "1.0"

scalaVersion := "2.9.1"

mainClass in Compile := Some("Importer")

libraryDependencies ++= Seq(
    "net.databinder.dispatch"   %% "dispatch-core"       % "0.9.4",
    "ch.qos.logback"            %  "logback-classic"     % "1.0.6",
    "com.weiglewilczek.slf4s"   %% "slf4s"               % "1.0.7"
    )

