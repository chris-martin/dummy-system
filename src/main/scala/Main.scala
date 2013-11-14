import com.typesafe.config.{Config, ConfigFactory}
import org.eclipse.jetty.server.Server

object Main {

  def main(args: Array[String]) {

    val config: Config = ConfigFactory.load()
    val server = new Server(config.getInt("dummy.port"))
    server.start()
    server.join()
    onShutdown(server.stop())
  }

  def onShutdown(f: => Unit): Unit =
    Runtime.getRuntime addShutdownHook newThread(f)

  def newThread(f: => Unit): Thread =
    new Thread(new Runnable { def run(): Unit = f })

}
