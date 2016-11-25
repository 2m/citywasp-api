package citywasp.api

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.Future

object CityWaspApiSpec {

  case class LocalCityWasp() extends CityWasp {
    def session = Future.successful(LocalSession())
  }

  case class LocalSession() extends Session {
    def loginChallenge = Future.successful(LocalLoginChallenge())
  }

  case class LocalLoginChallenge() extends LoginChallenge {
    def login = Future.successful(LocalLoggedIn())
  }

  case class LocalLoggedIn() extends LoggedIn {
    def currentCar = Future.successful(Some(LocalLockedCar()))
  }

  case class LocalLockedCar() extends LockedCar {
    def unlock = Future.successful(())
  }

  case class LocalUnlockedCar() extends UnlockedCar {
    def lock = Future.successful(())
  }

}

class CityWaspApiSpec extends WordSpec with ScalaFutures with Matchers {

  import CityWaspApiSpec._

  "city wasp" should {
    "work" in {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val cw = LocalCityWasp()

      val greeting = for {
        session   <- CityWasp.session
        challenge <- session.loginChallenge
        login     <- challenge.login
        status <- login.currentCar.map {
          case Some(c: LockedCar)   => "car locked"
          case Some(c: UnlockedCar) => "car unlocked"
          case None                 => "no car"
        }
      } yield status

      whenReady(greeting)(_ should be("car locked"))
    }
  }

}
