package network

import java.io.IOException

import JSON.{JSONHelper, Jfield}
import utils.Call

import scala.io.Source
import scala.util.parsing.json.JSON

/**
  * Created by radek on 01.06.17.
  */
class PowietrzeGisGovSource extends AirSource {
  def getAirData(city: String): Either[ConnectionError, AirData] = {

    val url = "http://powietrze.gios.gov.pl/pjp/current/getAQIDetailsList?param=AQI"

    Call.LOG("Connecting to " + url)

    try {
      val string = Source.fromURL(url).mkString

      Call.LOG("Success!")

      val json = JSON.parseFull(string)

      val air = new AirData


      json match {
        case Some(l: List[Any]) => l find {
          case m: Map[String, String] =>
            m.get("stationName") match {
              case None => false
              case Some(s: String) => s.contains(city)
            }
          case _ => false
        } match {
          case None => Right(air)
          case Some(map: Map[String, String]) =>
            air.co = JSONHelper.read(List("values", "CO").map(s => Jfield(s)))(map)
            air.no2 = JSONHelper.read(List("values", "NO2").map(s => Jfield(s)))(map)
            air.pm10 = JSONHelper.read(List("values", "PM10").map(s => Jfield(s)))(map)
            air.pm25 = JSONHelper.read(List("values", "PM2.5").map(s => Jfield(s)))(map)

            Right(air)

          case _ => Left(new ConnectionError("JSON error"))
        }
        case None => Left(new ConnectionError("JSON error"))
        case _ => Left(new ConnectionError("Unpredicted type received"))
      }
    }
    catch {
      case ioe: IOException => {
        Call.ERR("Connection error: " + ioe.getMessage)
        Left(new ConnectionError("Connection error: " + ioe.getMessage))
      }
    }
  }
}
