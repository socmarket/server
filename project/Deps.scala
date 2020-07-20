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
    val http4s         = "0.21.4"
    val circe          = "0.13.0"
    val quill          = "3.5.1"
    val doobie         = "0.9.0"
    val pureConfig     = "0.12.3"

    val h2             = "1.4.200"
    val logback        = "1.2.3"
  }

  object Lib {
    def circe(artifact: String) : ModuleID = "io.circe"     %% artifact % V.circe
    def http4s(artifact: String): ModuleID = "org.http4s"   %% artifact % V.http4s
    def zioM(artifact: String)  : ModuleID = "dev.zio"      %% artifact % V.zio
    def doobie(artifact: String): ModuleID = "org.tpolecat" %% artifact % V.doobie

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

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig

    val h2 = "com.h2database" % "h2" % V.h2

    val logback = "ch.qos.logback" % "logback-classic" % V.logback

    val zioTest    = zioM("zio-test")     % Test
    val zioTestSbt = zioM("zio-test-sbt") % Test
  }

  lazy val core = Seq(
    Lib.zio,
  )

  lazy val twoc = core ++ Seq(
    Lib.zio,
  )
}
