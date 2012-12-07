import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name := "Shopping List"

organization := "com.denner"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"       % "1.8" % "test",
  "net.liftweb"   %  "lift-json_2.9.1" % "2.4" % "compile->default"
)

scalacOptions := Seq("-Ydependent-method-types", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "io.spray"          %  "spray-routing" % "1.0-M5",
  "io.spray"          %  "spray-can"     % "1.0-M5",
  "io.spray"          %  "spray-httpx"   % "1.0-M5",
  "com.typesafe.akka" %  "akka-actor"    % "2.0.3",
  "io.spray"          %% "spray-json"    % "1.2.3"
)

resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.startscript" % "xsbt-start-script-plugin" % "0.5.3")
