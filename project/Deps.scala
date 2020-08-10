import sbt._

object Deps {

  val resolvers: Seq[Resolver] = Seq(
    Resolver.defaultLocal,
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
  )

  object V {
    val catsEffect     = "2.2.0-RC3"
    val fs2            = "2.4.2"
    val http4s         = "0.21.6"
    val circe          = "0.13.0"
    val circeConfig    = "0.8.0"
    val doobie         = "0.9.0"
    val pureConfig     = "0.12.3"
    val logback        = "1.2.3"
    val logstage       = "0.10.16"
    val uaParser       = "5.19"
  }

  object Lib {
    def fs2(artifact: String)      : ModuleID = "co.fs2"         %% ("fs2-" + artifact) % V.fs2
    def circe(artifact: String)    : ModuleID = "io.circe"       %% artifact % V.circe
    def http4s(artifact: String)   : ModuleID = "org.http4s"     %% artifact % V.http4s
    def doobie(artifact: String)   : ModuleID = "org.tpolecat"   %% ("doobie-" + artifact) % V.doobie
    def logstage(artifact: String) : ModuleID = "io.7mind.izumi" %% artifact % V.logstage

    val catsEffect   = "org.typelevel" %% "cats-effect" % V.catsEffect
    val fs2Core      = fs2("core")
    val fs2IO        = fs2("io")

    val http4sServer = http4s("http4s-blaze-server")
    val http4sDsl    = http4s("http4s-dsl")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce  = http4s("http4s-circe")

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeGExtras = circe("circe-generic-extras")
    val circeParser  = circe("circe-parser")
    val circeConfig  = "io.circe" %% "circe-config" % V.circeConfig

    val doobieCore   = doobie("core")
    val doobieQuill  = doobie("quill")
    val doobiePg     = doobie("postgres")
    val doobieHikari = doobie("hikari")

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig

    val logstageCore      = logstage("logstage-core")
    val logstageSlf4jSink = logstage("logstage-sink-slf4j")

    val uaParser = "nl.basjes.parse.useragent" % "yauaa" % V.uaParser

    val logback = "ch.qos.logback" % "logback-classic" % V.logback
  }

  lazy val core = Seq(
  )

  lazy val twoc = core ++ Seq(
    Lib.catsEffect,
    Lib.fs2Core,
    Lib.fs2IO,
    Lib.http4sServer,
    Lib.http4sDsl,
    Lib.http4sClient,
    Lib.http4sCirce,
    Lib.circeCore,
    Lib.circeParser,
    Lib.circeConfig,
    Lib.circeGeneric,
    Lib.circeGExtras,
    Lib.doobieCore,
    Lib.doobiePg,
    Lib.doobieHikari,
    Lib.logback,
    Lib.logstageCore,
    Lib.logstageSlf4jSink,
    Lib.uaParser,
  )
}
