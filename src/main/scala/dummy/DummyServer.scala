package dummy

import org.eclipse.jetty.server.{Request => JettyRequest, Handler => JettyHandler, Server => Jetty}
import org.eclipse.jetty.server.handler.{AbstractHandler => AbstractJettyHandler}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import HttpServletResponse.{SC_OK, SC_INTERNAL_SERVER_ERROR}
import scala.util.Random
import net.liftweb.json
import scalaj.http.{HttpOptions, Http}
import net.liftweb.json.Serialization
import scala.util.control.NonFatal
import scala.concurrent.duration._

object DummyServer extends Runnable {

  def run() {
    val config = dummy.config.getConfig("dummy.server")
    val port: Int = config getInt "port"
    val server = new DummyServer(port)
    addShutdownHook(server.jetty.stop())
    server.jetty.start()
    readLine()
    server.jetty.stop()
    server.jetty.join()
  }

  def addShutdownHook(f: => Unit): Unit =
    Runtime.getRuntime addShutdownHook newThread(f)

  def newThread(f: => Unit): Thread =
    new Thread(new Runnable { def run(): Unit = f })

  case class Request(dummyData: Option[String] = None, node: Node)

  case class Response(dummyData: Option[String] = None, errors: Seq[String] = Nil)

  case class Node(
    collaborators: Seq[Collaborator] = Nil,
    cpuIntensity: Option[Int] = None, // how much cpu consumption should occur
    responseSize: Option[Int] = None // how much data does this server return
  )

  case class Collaborator(
    url: String,
    node: Node,
    requestSize: Option[Int] = None // how much data should be sent to this service
  )

  def log(s: String): Unit = println(s)

}

import DummyServer._

class DummyServer(port: Int) {
  
  val jetty = new Jetty(port)
  jetty setHandler requestHandler

  val random = new Random

  implicit val formats = json.DefaultFormats

  def requestHandler: JettyHandler = new AbstractJettyHandler {

    def handle(target: String, baseRequest: JettyRequest,
    httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {

      def respond(statusCode: Int, r: Response) {
        httpResponse setContentType "application/json;charset=utf-8"
        httpResponse setStatus statusCode
        val responseString = Serialization.write(r)
        log(s"Response: $responseString")
        val w = httpResponse.getWriter
        w println responseString
        w.flush()
        w.close()
        baseRequest setHandled true
      }

      lazy val requestString = io.Source
        .fromInputStream(httpRequest.getInputStream)
        .getLines().mkString("\n")

      lazy val request: Request = json.parse(requestString).extract[Request]

      lazy val node: Node = request.node

      try {

        log(s"Request: $requestString")

        val cpuIntensity: Int = node.cpuIntensity getOrElse 100

        utilizeCpu(cpuIntensity)

        for (co <- node.collaborators) {
          val request = Request(
            dummyData = Some(arbitraryString(co.requestSize getOrElse 100)),
            node = co.node
          )
          Http.postData(co.url, Serialization.write(request))
            .options(
              HttpOptions.connTimeout(5.seconds.toMillis.toInt),
              HttpOptions.readTimeout(5.seconds.toMillis.toInt))
            .asString
        }

        utilizeCpu(cpuIntensity)

        respond(SC_OK, Response(
          dummyData = Some(arbitraryString(node.responseSize getOrElse 100))
        ))

      } catch { case NonFatal(e) =>

        e.printStackTrace()

        respond(SC_INTERNAL_SERVER_ERROR, Response(
          errors = Seq(s"${e.getMessage}")
        ))
      }
    }
  }

  def utilizeCpu(scale: Int): Unit =
    Stream.fill(scale * 1000)(BigInt(random.nextLong())).filter(_.isProbablePrime(50)).sum

  def arbitraryString(scale: Int): String =
    Stream.fill(scale * 100)(random.nextLong()).mkString(",")

}
