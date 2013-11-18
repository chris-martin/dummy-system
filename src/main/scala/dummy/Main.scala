package dummy

object Main {

  def main(args: Array[String]) {

    args(0) match {

      case "client" =>
        DummyClient.run()

      case "server" =>
        DummyServer.run()

    }
  }

}
