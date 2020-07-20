enablePlugins(GitPlugin)

ThisBuild / name         := "SocMarket Network Backend"
ThisBuild / maintainer   := "SocMarket Network Backend Development Team"
ThisBuild / organization := "io.github.socmarket"

val axisBranch  = settingKey[String]("Build branch, one of: dev, test, prod")
val axisVersion = settingKey[String]("Build version")
val axisCommit  = settingKey[String]("Build commit")
val axisTstamp  = settingKey[String]("Build timestamp")

ThisBuild / axisTstamp  := Version.timestamp
ThisBuild / axisBranch  := Version.getBranch(git.gitCurrentBranch.value)
ThisBuild / axisCommit  := Version.getCommit(git.gitHeadCommit.value)
ThisBuild / axisVersion := Version.getVersion(
  git.gitDescribedVersion.value,
  git.gitUncommittedChanges.value,
  axisBranch.value,
  axisCommit.value
)

ThisBuild / version      := axisVersion.value
ThisBuild / scalaVersion := Opts.Compiler.version
ThisBuild / logLevel     := Level.Info
ThisBuild / resolvers    := Deps.resolvers

lazy val `socmarket-core` = project
  .settings(Opts.core)
  .settings(libraryDependencies ++= Deps.core)

lazy val `socmarket-twoc` = project
  .enablePlugins(
    JavaAppPackaging,
  )
  .dependsOn(
    `socmarket-core`,
  )
  .settings(Opts.twoc)
  .settings(libraryDependencies ++= Deps.twoc)

lazy val SocMarket = (project in file("."))
  .aggregate(
    `socmarket-core`,
    `socmarket-twoc`,
  )
  .settings(
    crossScalaVersions := Nil,
    publish := {}
  )
