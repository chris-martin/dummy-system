import com.typesafe.config.{Config => TypesafeConfig}
import edu.gatech.gtri.typesafeconfigextensions.factory.ConfigFactory, ConfigFactory._
import java.nio.file.Paths.{get => path}

package object dummy {

  def configFactory: ConfigFactory =
    emptyConfigFactory
      .bindDefaults()
      .withSources(
        classpathResource("reference"),
        classpathResource("application"),
        systemProperties(),
        configFile().byPath(path("dummy.conf"))
      ).fromLowestToHighestPrecedence()

  def config: TypesafeConfig = configFactory.load

}
