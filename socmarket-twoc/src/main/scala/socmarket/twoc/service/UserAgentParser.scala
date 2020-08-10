package socmarket.twoc.service

import socmarket.twoc.adt.auth.UserAgent

import cats.effect.{ContextShift, Resource, Sync}
import logstage.LogIO
import nl.basjes.parse.useragent.UserAgentAnalyzer
import cats.syntax.functor._

import scala.concurrent.ExecutionContext

object UserAgentParser {

  trait Service[F[_]] {
    def parse(ua: String): F[UserAgent]
  }

  def createService[F[_]: Sync: ContextShift: LogIO](ec: ExecutionContext): Resource[F, Service[F]] = {
    val parser = UserAgentAnalyzer
      .newBuilder()
      .hideMatcherLoadStats()
      .withCache(10000)
      .build();
    Resource.make(Sync[F].delay(create(ec, parser)))(_ => Sync[F].delay(()))
  }

  private def create[F[_]: Sync: ContextShift: LogIO](ec: ExecutionContext, parser: UserAgentAnalyzer): Service[F] = {
    new Service[F] {
      private val cs = implicitly[ContextShift[F]]
      private val F = implicitly[Sync[F]]
      def parse(ua: String): F[UserAgent] = {
        val job = F.delay(parser.parse(ua)).map(ua => UserAgent(
        ))
        cs.evalOn(ec)(job)
      }
    }
  }

}