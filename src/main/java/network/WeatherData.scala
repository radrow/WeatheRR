package network

/**
  * Created by radek on 01.06.17.
  * Class containing weather info
  */
class WeatherData(var temperature: String
                  , var barometer: String
                  , var weather: String
                  , var windDeg: String
                  , var windSpeed: String
                  , var humidity: String
                  , var visibility: String
                  , var dewPoint: String
                  , var feelsLike: String
                 ) {

  def this() = this("-", "-", "-", "-", "-", "-", "-", "-", "-")

  override def toString: String =
    "It is " + weather + ", it's " + temperature + " degrees (feels like " + feelsLike + ") and pressure is " + barometer +
      ". The wind blows at " + windDeg + " degrees with " + windSpeed + " speed." +
      "The humidity reaches " + humidity + " + and dew point " + dewPoint + ". Visibility is about " + visibility
}