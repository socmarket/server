package socmarket.twoc.ext

import socmarket.twoc.config.NexmoConf
import logstage.LogIO
import logstage.LogIO.log
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl._
import org.http4s.Method._
import org.http4s.{Uri, UrlForm}
import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.functor._
import cats.syntax.flatMap._
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.Configuration
import io.circe.Decoder
import socmarket.twoc.api.ApiErrorExternal

object Nexmo {

  case class NexmoMessage(
    to: String,
    messageId: String,
    status: String,
    remainingBalance: String,
    messagePrice: String,
    network: String,
  )

  case class SendSmsRes(messageCount: Int = 0, messages: List[NexmoMessage] = List.empty)

  object SendSmsRes {
    private implicit val config: Configuration = Configuration
      .default
      .withKebabCaseMemberNames
      .withKebabCaseConstructorNames
    implicit val decoder: Decoder[SendSmsRes] = deriveConfiguredDecoder
  }

  object NexmoMessage {
    private implicit val config: Configuration = Configuration
      .default
      .withKebabCaseMemberNames
      .withKebabCaseConstructorNames
    implicit val decoder: Decoder[NexmoMessage] = deriveConfiguredDecoder
  }

  trait Service[F[_]] {
    def sendSms(msisdn: Long, text: String): F[NexmoMessage]
    def sendVoice(msisdn: Long, text: String): F[Unit]
  }

  def createService[F[_]: ConcurrentEffect: LogIO](http: Client[F], conf: NexmoConf): Resource[F, Service[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(http, conf)))(_ => ConcurrentEffect[F].delay(()))

  private def create[F[_]: ConcurrentEffect: LogIO](http: Client[F], conf: NexmoConf): Service[F] = new Service[F] {

    private val F = implicitly[ConcurrentEffect[F]]
    import org.http4s.circe.CirceEntityDecoder._

    def sendSms(msisdn: Long, text: String): F[NexmoMessage] = {
      val data = UrlForm(
        "api_key"    -> conf.apiKey,
        "api_secret" -> conf.apiSecret,
        "from"       -> conf.from,
        "to"         -> s"+${msisdn.toString}",
        "text"       -> text,
      )
      for {
        url <- F.delay(Uri.unsafeFromString(conf.sendSmsUrl))
        res <- http.expect[SendSmsRes](POST.apply(data, url))
        msg <- F.fromOption(res.messages.headOption, ApiErrorExternal("Nexmo returned empty messages list"))
      } yield msg
    }

    override def sendVoice(msisdn: Long, text: String): F[Unit] = ???
  }

}