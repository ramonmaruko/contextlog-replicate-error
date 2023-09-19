package com.example.quickstart

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = QuickstartServer.run[IO]
}
