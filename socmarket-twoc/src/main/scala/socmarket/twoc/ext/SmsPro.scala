package socmarket.twoc.ext

import socmarket.twoc.config.SmsProConf
import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.either._
import logstage.LogIO
import logstage.LogIO.log
import org.http4s.client.dsl.Http4sClientDsl._
import org.http4s.Method._
import org.http4s.client.Client
import org.http4s.{Charset, MediaType, Uri}
import org.http4s.headers.`Content-Type`
import ru.tinkoff.phobos.Namespace
import ru.tinkoff.phobos.decoding._
import ru.tinkoff.phobos.derivation.semiauto._
import ru.tinkoff.phobos.syntax.xmlns

object SmsPro {

  case class SmsProResponse(
    @xmlns(SmsProResponse.GiperMobiNs) id: String  = "",
    @xmlns(SmsProResponse.GiperMobiNs) status: Int = 0,
    @xmlns(SmsProResponse.GiperMobiNs) phones: Int = 0,
    @xmlns(SmsProResponse.GiperMobiNs) smscnt: Int = 0,
    @xmlns(SmsProResponse.GiperMobiNs) message: Option[String] = None,
  )

  object SmsProResponse {
    object GiperMobiNs {
      implicit val ns: Namespace[GiperMobiNs.type] = Namespace.mkInstance("http://Giper.mobi/schema/Message")
    }
    implicit val decoder: XmlDecoder[SmsProResponse] = deriveXmlDecoder("response", GiperMobiNs)
  }

  trait Service[F[_]] {
    def sendSms(id: Int, msisdn: Long, text: String): F[String]
    def sendVoice(msisdn: Long, text: String): F[Unit]
  }

  def createService[F[_]: ConcurrentEffect: LogIO](http: Client[F], conf: SmsProConf): Resource[F, Service[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(http, conf)))(_ => ConcurrentEffect[F].delay(()))

  private def create[F[_]: ConcurrentEffect: LogIO](http: Client[F], conf: SmsProConf): Service[F] = new Service[F] {

    private val F = implicitly[ConcurrentEffect[F]]

    def sendSms(id: Int, msisdn: Long, text: String): F[String] = {
      val body = mkMessage(id, msisdn, text, conf)
      val res = for {
        url <- F.delay(Uri.unsafeFromString(conf.sendSmsUrl))
        raw <- http.expect[String](POST.apply(body, url, `Content-Type`(MediaType.application.`xml`, Charset.`UTF-8`)))
        xml = XmlDecoder[SmsProResponse]
                .decode(raw)
                .fold(err => SmsProResponse(message = Some(err.text)), identity)
        _ <- log.debug(s"SMS send request through SmsPro: $text")
        _ <- log.debug(s"SmsPro response: $xml")
      } yield xml
      res.map(_.id)
    }

    override def sendVoice(msisdn: Long, text: String): F[Unit] = ???
  }

  private def mkMessage(id: Int, msisdn: Long, text: String, conf: SmsProConf): String = {
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<message>
       |  <login>${conf.login}</login>
       |  <pwd>${conf.pass}</pwd>
       |  <id>$id</id>
       |  <sender>${conf.from}</sender>
       |  <text>$text</text>
       |  <phones>
       |    <phone>$msisdn</phone>
       |  </phones>
       |  <test>0</test>
       |</message>
    |""".stripMargin
  }

}