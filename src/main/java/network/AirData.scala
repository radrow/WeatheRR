package network

/**
  * Created by radek on 01.06.17.
  * Class that contains information about air quality
  */
class AirData(var co: String, var pm10: String, var pm25: String, var no2: String) {

  def this() = this("-", "-", "-", "-")

}
