package network

/**
  * Created by radek on 01.06.17.
  * Interface for any weather sources
  */
trait WeatherSource {
  /**
    * Returns information about weather
    * @param city base city
    * @return Right Weather when connection succeded, Left ConnectionError
    *         on failure
    */
  def getWeatherData(city: String): Either[ConnectionError, WeatherData]
}
