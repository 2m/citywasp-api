package citywasp.api

import scala.concurrent.Future

object CityWasp {
  def apply(implicit cw: CityWasp): Future[CityWasp] = Future.successful(cw)
}

trait CityWasp {
  def login: Future[LoggedIn]
}

trait LoggedIn {
  def availableCars: Future[Seq[Car]]
  def carsDetails: Future[Seq[CarDetails]]
}

case class Car(id: Int, lat: Double, lon: Double)
case class CarDetails(id: Int, licensePlate: String, brand: String, model: String)
