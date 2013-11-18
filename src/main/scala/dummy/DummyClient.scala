package dummy

import akka.actor._
import com.typesafe.config.{Config => TypesafeConfig, ConfigRenderOptions}
import scalaj.http.{HttpOptions, Http}
import scala.concurrent.duration._
import scala.collection.convert.wrapAll._
import scala.collection.mutable

object DummyClient extends Runnable {

  def run() {
    val actorSystem = ActorSystem()
    actorSystem.actorOf(Props(classOf[DummyClient], dummy.config))
    readLine()
    actorSystem.shutdown()
  }

  class Ensemble(config: TypesafeConfig) {

    def name = config getString "name"

    def delay: FiniteDuration = (config getNanoseconds "delay").toLong.nanos

    def url = config getString "url"

    def body: String = (config getObject "body")
      .render(ConfigRenderOptions.defaults.setJson(true).setOriginComments(false))
  }

  class HttpActor(e: Ensemble) extends Actor {

    def receive = {
      case 'init =>

        print(e.name)

        Http.postData(e.url, e.body)
          .options(
            HttpOptions.connTimeout(5.seconds.toMillis.toInt),
            HttpOptions.readTimeout(5.seconds.toMillis.toInt))
          .asString

        context stop self
    }
  }

}

import DummyClient._

class DummyClient(config: TypesafeConfig = dummy.config) extends Actor {

  val ensembles: Seq[Ensemble] =
    config.getConfigList("dummy.client.ensemble").map(new Ensemble(_))

  self ! 'init

  val http = mutable.Map[ActorRef, Ensemble]()

  override def supervisorStrategy: SupervisorStrategy =
    SupervisorStrategy.stoppingStrategy

  def receive = {

    case 'init =>
      ensembles.foreach(setTimer)

    case e: Ensemble =>
      val ref: ActorRef = context actorOf Props(classOf[HttpActor], e)
      context watch ref
      http.put(ref, e)
      ref ! 'init

    case Terminated(ref) =>
      val e = http.remove(ref).get
      setTimer(e)

  }

  def setTimer(e: Ensemble) {
    val system = context.system
    import system._
    scheduler.scheduleOnce(e.delay) { self ! e }
    e.delay
  }

}
