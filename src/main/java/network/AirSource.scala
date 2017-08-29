package network

/**
  * Created by radek on 01.06.17.
  * Interface for any air quality information sources
  */
trait AirSource {
  /**
    * Returns information about air quality
    * @param city base city
    * @return Right AirData when connection succeded, Left ConnectionError
    *         on failure
    */
  def getAirData(city: String): Either[ConnectionError, AirData]
}
