package citywasp.api

import scala.concurrent.Future

object CityWasp {
  def session(implicit cw: CityWasp): Future[Session] = cw.session
}

trait CityWasp {
  def session: Future[Session]
}

trait Session {
  def loginChallenge: Future[LoginChallenge]
}

trait LoginChallenge {
  def login: Future[LoggedIn]
}

trait LoggedIn {
  def currentCar: Future[Option[Car]]
  def parkedCars: Future[Seq[ParkedCar]]
}

sealed trait Car

case class ParkedCar(id: Int,
                     licensePlate: String,
                     brand: String,
                     model: String,
                     lat: Double,
                     lon: Double)

trait LockedCar extends Car {
  def unlock: Future[Unit]
}

trait UnlockedCar extends Car {
  def lock: Future[Unit]
}
