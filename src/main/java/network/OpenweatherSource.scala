package network

import java.io.IOException
import javafx.scene.image.Image

import JSON.{IntOrString, JSONHelper}
import utils.Call

import scala.io.Source
import scala.util.parsing.json._

/**
  * Created by radek on 01.06.17.
  */
class OpenweatherSource extends WeatherSource {

  /**
    * APPID of subscription created for purposes of this app
    */
  val APPID ="11b6cc57da4d68b1820c572c949d711b"

  def getWeatherData(city: String): Either[ConnectionError, WeatherData] = {

    val url = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&APPID="+APPID

    Call.LOG("Connecting to " + url)

    try {
      val received = Source.fromURL(url).mkString

      Call.LOG("Success!")

      val json = JSON.parseFull(received)

      val weather = new WeatherData

      json match {
        case None => Left(new ConnectionError("JSON error"))
        case Some(map: Map[String, String]) =>

          def pack(x: Any) = IntOrString.pack(x)

          weather.temperature = JSONHelper.read(List("list", 0, "main", "temp") map pack)(map)
          weather.barometer = JSONHelper.read(List("list", 0, "main", "pressure") map pack)(map)
          weather.weather = JSONHelper.read(List("list", 0, "weather", 0, "main") map pack)(map)
          weather.windDeg = JSONHelper.read(List("list", 0, "wind", "deg") map pack)(map)
          weather.windSpeed = JSONHelper.read(List("list", 0, "wind", "speed") map pack)(map)
          weather.humidity = JSONHelper.read(List("list", 0, "main", "humidity") map pack)(map)
          weather.visibility = JSONHelper.read(List("list", 0, "visibility") map pack)(map)
          weather.feelsLike = JSONHelper.read(List("list", 0, "main", "temp") map pack)(map)

          weather.feelsLike = try {
            (weather.feelsLike.toDouble.toInt - 273).toString
          }
          catch {
            case _: Exception => weather.feelsLike
          }

          weather.temperature = try {
            (weather.temperature.toDouble.toInt - 273).toString
          }
          catch {
            case _: Exception => weather.feelsLike
          }

          Right(weather)

        case _ => Left(new ConnectionError("JSON error"))
      }
    } catch {
      case ioe: IOException =>
        Call.ERR("Connection error: " + ioe.getMessage)
        Left(new ConnectionError("Connection error: " + ioe.getMessage))
    }
  }
}

/**
  * For global features
  */
object OpenweatherSource {
  /**
    * Returns proper image representing weather in given city
    */
  def getWeatherIcon(city: String): Either[ConnectionError, Image] = {

    val url = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&APPID=11b6cc57da4d68b1820c572c949d711b"
    try {
      Call.LOG("Connecting to " + url)
      val string = Source.fromURL(url).mkString

      Call.LOG("Success!")

      val json = JSON.parseFull(string)

      def pack(x: Any) = IntOrString.pack(x)

      json match {
        case None => Left(new ConnectionError("JSON error"))
        case Some(map: Map[String, String]) =>
          val imgId = JSONHelper.read(List("list", 0, "weather", 0, "icon") map pack)(map)
          val imageUrl = "http://openweathermap.org/img/w/" + imgId + ".png"
          try {
            Right(new Image(imageUrl))
          } catch {
            case _: Exception => Left(new ConnectionError("Image load error"))
          }
        case _ => Left(new ConnectionError("JSON error"))
      }
    } catch {
      case _: IOException => Left(new ConnectionError("Could not connect"))
    }
  }
}