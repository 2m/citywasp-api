package citywasp.api

import java.util.concurrent.atomic.AtomicInteger
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config._
import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.Await
import scala.concurrent.duration._

class RemoteCityWaspApiSpec extends WordSpec with Matchers with ScalaFutures {

  val id = new AtomicInteger(0)

  sealed trait CarStatus
  object Reserved extends CarStatus
  object Unlocked extends CarStatus
  object NoCar    extends CarStatus
  object Error    extends CarStatus

  def withServer(allowLogin: Boolean = true, carStatus: CarStatus = Reserved, allowLock: Boolean = true, allowUnlock: Boolean = true)(testCode: Config => Any) {
    implicit val sys = ActorSystem(s"RemoteCityWaspApiSpec-${id.addAndGet(1)}")
    implicit val mat = ActorMaterializer()

    val config: Config = ConfigFactory.parseString("""
      citywasp {
        session-cookie = "test_cookie"
        user-agent = "test_agent"
        email = "test@email.com"
        password = "test_password"
      }
    """)

    val route =
      path("/lt/auth/") {
        get {
          optionalCookie(config.getString("citywasp.session-cookie")) {
            case Some(cookie) => {
              implicit val mar = ScalaXmlSupport.nodeSeqMarshaller(MediaTypes.`text/html`)
              complete {
                <html>
								  <body>
 									  <form name="login">
            				  <input name="login[_token]" value="c62ETPyxEh3t2QkZsxjPGaD9cEfgqG668OEjYDDQinw" />
        					  </form>
								  </body>
							  </html>
              }
            }
            case None => setCookie(HttpCookie(config.getString("citywasp.session-cookie"), value = "test_session_id"))(complete("Session created."))
          }
        }
      } ~
      path("/lt/auth/login/") {
        post {
          formFields("login[_token]", "login[email]", "login[password]") { (token, email, password) =>
            val location = if (allowLogin) "/lt/?showInfo=1" else "/lt/auth/"
            redirect(location, StatusCodes.Found)
          }
        }
      } ~
      path("/lt/reservation/active") {
        implicit val mar = ScalaXmlSupport.nodeSeqMarshaller(MediaTypes.`text/html`)
        carStatus match {
          case Reserved => complete {
            <html>
							<body>
              	<script type="text/javascript">
                	var currentTime = 833000;
              	</script>
              	<div>
                	<a href="/lt/car/unlock/177" />
              	</div>
            	</body>
						</html>
          }
          case Unlocked => complete {
            <html>
						  <body>
                <div>
                  <a href="/lt/car/lock/177" />
                </div>
							</body>
						</html>
          }
          case NoCar => complete {
            <html>
							<body>
								<div class="error-msg">Duomenų nėra</div>
							</body>
						</html>
          }
          case Error => complete {
            <html></html>
          }
        }
      } ~
      path("lt" / "car" / "unlock" / IntNumber) { carId =>
        if (allowUnlock) redirect("/lt/reservation/active", StatusCodes.Found) else complete("Ohi")
      } ~
      path("lt" / "car" / "lock" / IntNumber) { carId =>
        if (allowLock) redirect("/lt/", StatusCodes.Found) else complete("Ohi")
      }

    val binding = Await.result(Http().bindAndHandle(route, "localhost", 0), 1.second)

    try {
      testCode(ConfigFactory
          .parseString(s"""citywasp.url = "http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}"""")
          .withFallback(config)
          .getConfig("citywasp"))
    }
    finally {
      Await.ready(binding.unbind(), 1.second)
      sys.shutdown()
      sys.awaitTermination()
    }
  }

  "city wasp" should {

    "aquire session" in withServer() { config =>
      implicit val cw = RemoteCityWasp(config)
      Await.result(CityWasp.session, 1.second) shouldBe a[Session]
    }

    "aquire login challenge" in withServer() { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
      } yield challenge
      Await.result(result, 1.second) shouldBe a[LoginChallenge]
    }

    "successfuly login" in withServer() { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
      } yield loggedIn
      Await.result(result, 1.second) shouldBe a[LoggedIn]
    }

    "fail when logging in" in withServer(allowLogin = false) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
      } yield loggedIn
      Await.result(result.failed, 1.second).getCause.getMessage shouldBe "Unable to log in. Check username/password."
    }

    "get reserved car" in withServer(carStatus = Reserved) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        Some(car) <- loggedIn.currentCar
      } yield car
      Await.result(result, 1.second) shouldBe a[LockedCar]
    }

    "get unlocked car" in withServer(carStatus = Unlocked) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        Some(car) <- loggedIn.currentCar
      } yield car
      Await.result(result, 1.second) shouldBe a[UnlockedCar]
    }

    "return no car" in withServer(carStatus = NoCar) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        car <- loggedIn.currentCar
      } yield car
      Await.result(result, 1.second) shouldBe None
    }

    "report error when getting car" in withServer(carStatus = Error) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        car <- loggedIn.currentCar
      } yield car
      Await.result(result.failed, 1.second).getCause.getMessage shouldBe "Error while getting current car."
    }

    "unlock car" in withServer(carStatus = Reserved, allowUnlock = true) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        Some(car: LockedCar) <- loggedIn.currentCar
        res <- car.unlock
      } yield res
      Await.result(result, 1.second) shouldBe ()
    }

    "report error when unlocking car" in withServer(carStatus = Reserved, allowUnlock = false) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        Some(car: LockedCar) <- loggedIn.currentCar
        res <- car.unlock
      } yield res
      Await.result(result.failed, 1.second).getCause.getMessage shouldBe "Error while unlocking current car."
    }

    "lock car" in withServer(carStatus = Unlocked, allowLock = true) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        Some(car: UnlockedCar) <- loggedIn.currentCar
        res <- car.lock
      } yield res
      Await.result(result, 1.second) shouldBe ()
    }

    "report error when locking car" in withServer(carStatus = Unlocked, allowLock = false) { config =>
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = RemoteCityWasp(config)
      val result = for {
        session <- CityWasp.session
        challenge <- session.loginChallenge
        loggedIn <- challenge.login
        Some(car: UnlockedCar) <- loggedIn.currentCar
        res <- car.lock
      } yield res
      Await.result(result.failed, 1.second).getCause.getMessage shouldBe "Error while locking current car."
    }
  }
}
