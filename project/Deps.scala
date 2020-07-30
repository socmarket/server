import sbt._

object Deps {

  val resolvers: Seq[Resolver] = Seq(
    Resolver.defaultLocal,
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
  )

  object V {
    val zio            = "1.0.0-RC20"
    val zioInteropCats = "2.1.3.0-RC15"
    val zioLogging     = "0.3.1"
    val http4s         = "0.21.6"
    val circe          = "0.13.0"
    val quill          = "3.5.1"
    val doobie         = "0.9.0"
    val pureConfig     = "0.12.3"
    val h2             = "1.4.200"
    val logback        = "1.2.3"
    val logstage       = "0.10.16"
    val tofu           = "0.7.8"
  }

  object Lib {
    def circe(artifact: String)   : ModuleID = "io.circe"       %% artifact % V.circe
    def http4s(artifact: String)  : ModuleID = "org.http4s"     %% artifact % V.http4s
    def zioM(artifact: String)    : ModuleID = "dev.zio"        %% artifact % V.zio
    def doobie(artifact: String)  : ModuleID = "org.tpolecat"   %% artifact % V.doobie
    def logstage(artifact: String): ModuleID = "io.7mind.izumi" %% artifact % V.logstage
    def tofu(artifact: String)    : ModuleID =  "ru.tinkoff"    %% ("tofu-" + artifact) % V.tofu

    val zio            = zioM("zio")
    val zioStreams     = zioM("zio-streams")
    val zioMacros      = zioM("zio-macros")
    val zioInteropCats = "dev.zio" %% "zio-interop-cats" % V.zioInteropCats
    val zioLogging     = "dev.zio" %% "zio-logging"      % V.zioLogging

    val http4sServer = http4s("http4s-blaze-server")
    val http4sDsl    = http4s("http4s-dsl")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce  = http4s("http4s-circe")

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser  = circe("circe-parser")

    val quillJdbc   = "io.getquill" %% "quill-jdbc" % V.quill
    val doobieCore  = doobie("doobie-core")
    val doobieQuill = doobie("doobie-quill")
    val doobieH2    = doobie("doobie-h2")
    val doobiePg    = doobie("doobie-postgres")

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig

    val h2 = "com.h2database" % "h2" % V.h2

    val logstageCore      = logstage("logstage-core")
    val logstageSlf4jSink = logstage("logstage-sink-slf4j")

    val logback = "ch.qos.logback" % "logback-classic" % V.logback

    val tofuDoobie  = tofu("doobie")
    val tofuZioCore = tofu("zio-core")

    val zioTest    = zioM("zio-test")     % Test
    val zioTestSbt = zioM("zio-test-sbt") % Test
  }

  lazy val core = Seq(
  )

  lazy val twoc = core ++ Seq(
    Lib.zio,
    Lib.zioStreams,
    Lib.zioMacros,
    Lib.zioInteropCats,
    Lib.zioLogging,
    Lib.http4sServer,
    Lib.http4sDsl,
    Lib.http4sClient,
    Lib.http4sCirce,
    Lib.circeCore,
    Lib.circeGeneric,
    Lib.circeParser,
    Lib.quillJdbc,
    Lib.doobieCore,
    Lib.doobieQuill,
    Lib.doobiePg,
    Lib.pureConfig,
    Lib.h2,
    Lib.logback,
    Lib.logstageCore,
    Lib.logstageSlf4jSink,
    Lib.tofuDoobie,
    Lib.tofuZioCore,
    Lib.zioTestSbt,
  )
}
