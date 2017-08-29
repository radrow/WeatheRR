package network

import java.io.IOException

import utils.Call

import scala.io.Source

/**
  * Created by radek on 01.06.17.
  */
class MeteoSource extends WeatherSource {
  def getWeatherData(city: String): Either[ConnectionError, WeatherData] = {
    val url = "http://www.meteo.waw.pl/"
    try {
      val html = Source.fromURL(url).mkString

      Call.LOG("Success!")

      val weather = new WeatherData

      /**
        * Returns substring from selected area
        * @param from beginning (not included)
        * @param to end (not inluded)
        * @param input base string
        * @return string between `from` and `to` first occurrences,
        *         or empty string if not found
        */
      def extract(from: String)(to: String)(input: String): String = {
        val pac = input.split(from)
        Call.DBG("The " + from + " found " + (pac.length - 1) + " times")
        if (pac.length < 2) ""
        else pac(1).split(to)(0)
      }

      /**
        * Removes substring from input
        * @param from beginning of removal (included)
        * @param to end of removal (included)
        * @param input base string
        * @return string without substring between `from` and `to`
        */
      def cut(from: String)(to: String)(input: String): String = {
        val pac = input.split(from)
        val pre = pac(0)
        val post = pac(1).split(to)(1)
        pre + post
      }

      // Part containing data we are interested
      val dataZone = extract("<div id=\"msr_short\">")("</div>")(html)
      Call.DBG("Splitted out " + dataZone)

      // Data exctraction
      weather.temperature = (extract("temperatura ")("</span>") _ andThen cut("<")(">")) (dataZone)
      weather.barometer = (extract("ciśnienie ")("</span>") _ andThen cut("<")(">")) (dataZone)
      weather.feelsLike = (extract("odczuwalna ")("</span>") _ andThen cut("<")(">")) (dataZone)
      weather.humidity = (extract("wilgotność ")("</span>") _ andThen cut("<")(">")) (dataZone)
      weather.windSpeed = (extract("wiatr ")("</span>") _ andThen cut("<")(">")) (dataZone)
      weather.windDeg = extract("<span id=\"PARAM_0_WDABBR\">")("</span>")(dataZone)

      weather.temperature = weather.temperature.replaceAll(",", ".")
      weather.feelsLike = weather.feelsLike.replaceAll(",", ".")

      weather.feelsLike = try
        weather.feelsLike.toDouble.toInt.toString
      catch {case _: Exception => weather.feelsLike}

      weather.temperature = try
        weather.temperature.toDouble.toInt.toString
      catch {case _: Exception => weather.feelsLike}

      Right(weather)

    } catch {
      case ioe: IOException =>
        Call.ERR("Connection error: " + ioe.getMessage)
        Left(new ConnectionError("Connection error: " + ioe.getMessage))
    }
  }
}
