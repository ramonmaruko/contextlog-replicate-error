package com.example.quickstart

import cats.effect.Async
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{Logger => ServerLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.Resource
import io.chrisdavenport.http4s.log4cats.contextlog.ClientMiddleware

object QuickstartServer {
  def run[F[_]: Async: Network]: F[Nothing] = {
    for {
      logger <- Resource.eval(Slf4jLogger.create[F])
      client <- EmberClientBuilder.default[F].build.map(ClientMiddleware.fromLogger(logger).client)
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        QuickstartRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        QuickstartRoutes.jokeRoutes[F](jokeAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = ServerLogger.httpApp(true, true)(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
