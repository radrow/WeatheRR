package weatherr

import java.util.Calendar
import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.concurrent.{Service, Task}
import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent

import network._
import utils.Call


/**
  * Created by radek on 31.05.17.
  */
class Controller() {

  val cityEN = "Warsaw"
  val cityPL = "Warszawa"

  val TIMEOUT = 10
  val AUTO_REFRESH_PERIOD = 300000 // 5 min

  val autoRefreshThread = new Thread() {
    override def run(): Unit = {
      while (true) {
        try {
          Thread.sleep(AUTO_REFRESH_PERIOD)
        } catch {
          case _: InterruptedException => ()
        }
        refresh()
      }
    }
  }


  var currentWeatherSource: WeatherSource = new OpenweatherSource
  var currentAirSource: AirSource = new PowietrzeGisGovSource

  @FXML
  var exitButton: Button = _
  @FXML
  var refreshButton: Button = _

  @FXML
  var feelsLike: Label = _
  @FXML
  var barometer: Label = _
  @FXML
  var wind: Label = _
  @FXML
  var humidity: Label = _
  @FXML
  var visibility: Label = _
  @FXML
  var dewPoint: Label = _

  @FXML
  var updatedAsOf: Label = _
  @FXML
  var weather: Label = _
  @FXML
  var temperature: Label = _
  @FXML
  var myLocation: Label = _

  @FXML
  var no2: Label = _
  @FXML
  var co: Label = _
  @FXML
  var pm10: Label = _
  @FXML
  var pm25: Label = _

  @FXML
  var meteo: RadioButton = _
  @FXML
  var openWeather: RadioButton = _
  var serverGroup: ToggleGroup = new ToggleGroup

  @FXML
  var weatherIcon: ImageView = _

  @FXML
  def initialize(): Unit = {

    refresh()
    autoRefreshThread.start()

    exitButton.setOnAction(new EventHandler[ActionEvent] {
      override def handle(t: ActionEvent): Unit = System.exit(0)
    })

    refreshButton.setOnMouseClicked(new EventHandler[MouseEvent] {
      override def handle(t: MouseEvent): Unit = refresh()
    })

    meteo.setUserData(new MeteoSource)
    openWeather.setUserData(new OpenweatherSource)

    meteo.setToggleGroup(serverGroup)
    openWeather.setToggleGroup(serverGroup)

    // set default weather source
    openWeather.fire()

    serverGroup.selectedToggleProperty.addListener(new ChangeListener[Toggle]() {
      override def changed(ov: ObservableValue[_ <: Toggle], old_toggle: Toggle, new_toggle: Toggle): Unit = {
        if (serverGroup.getSelectedToggle != null) {
          serverGroup.getSelectedToggle.getUserData match {
            case ws: WeatherSource => setWeatherSource(ws)
            case _ => throw new RuntimeException("Bad weather source on radio button")
          }
          refresh()
        }
      }
    })

  }

  /**
    * Used to prevent multiple refreshing at the same time
    */
  var refreshing = false

  private def refresh() = {
    if (refreshing) {
      Call.LOG("Refresh skipped.")
    } else {
      refreshing = true
      val controlThread = new Thread() {
        override def run() {
          val refreshT = new Thread() {
            override def run(): Unit = {

              var weather = new WeatherData()
              var icon: Image = null
              var air = new AirData()

              var error = false

              currentAirSource.getAirData(cityPL) match {
                case Right(data) => air = data
                case Left(e: ConnectionError) => Call.ERR(e.msg); error = true
              }
              OpenweatherSource.getWeatherIcon(cityEN) match {
                case Right(img) => icon = img
                case Left(e: ConnectionError) => Call.ERR(e.msg); error = true
              }
              currentWeatherSource.getWeatherData(cityEN) match {
                case Right(data) => weather = data
                case Left(e: ConnectionError) => Call.ERR(e.msg); error = true
              }

              new Service[Unit]() {
                override def createTask(): Task[Unit] = {
                  new Task[Unit]() {
                    override def call(): Unit = {
                      Platform.runLater(new Runnable {
                        override def run(): Unit = {
                          updateWeatherIcon(icon)
                          updateWeather(weather, cityEN)
                          updateAir(air)
                          if (error) updatedAsOf.setText("Could not connect")
                        }
                      })
                    }
                  }
                }
              }.start()
            }
          }
          val time = System.nanoTime()
          refreshT.start()
          while (refreshT.isAlive) {
            try {
              if (System.nanoTime() - time > 10000000000L) refreshT.interrupt()
              Thread.sleep(50)
            } catch {
              case _: InterruptedException => ()
            }
          }
          refreshing = false
        }
      }
      controlThread.start()
    }
  }

  private def updateWeather(data: WeatherData, city: String) = {
    updateBarometerText(data)
    updateDewPointText(data)
    updateFeelsLikeText(data)
    updateHumidityText(data)
    updateLocationText(city)
    updateTemperatureText(data)
    updateVisibilityText(data)
    updateWeatherText(data)
    updateWindText(data)
    updateUpdatedAsOfText()
  }

  private def updateAir(data: AirData): Unit = {
    updatePM10(data)
    updatePM25(data)
    updateNO2(data)
    updateCO(data)
  }

  private def updateWeatherIcon(img: Image): Unit = {
    weatherIcon.setImage(img)
  }

  private def isValidData(s: String): Boolean = s.forall(c => c.isLetterOrDigit || c == '.' || c == ',') && !s.isEmpty

  private def updateFeelsLikeText(data: WeatherData): Unit = {
    feelsLike.setText("Feels like " + data.feelsLike +  "°C")
  }

  private def updateBarometerText(data: WeatherData): Unit = {
    barometer.setText("Barometer " + data.barometer +
      (if (isValidData(data.barometer)) " mb" else ""))
  }

  private def updateWindText(data: WeatherData): Unit = {
    wind.setText("Wind deg: " + data.windDeg +
      (if (isValidData(data.windDeg)) "°" else "")) + ", speed: " +
      data.windSpeed + (if (isValidData(data.windSpeed)) " km/h" else "")
  }

  private def updateHumidityText(data: WeatherData): Unit = {
    humidity.setText("Humidity " + data.humidity +
      (if (isValidData(data.humidity)) "%" else ""))
  }

  private def updateVisibilityText(data: WeatherData): Unit = {
    visibility.setText("Visibility " + data.visibility +
      (if (isValidData(data.visibility)) "m" else ""))
  }

  private def updateDewPointText(data: WeatherData): Unit = {
    dewPoint.setText("Dew point " + data.dewPoint +
      (if (isValidData(data.dewPoint)) "°" else ""))
  }

  private def updateTemperatureText(data: WeatherData): Unit = {
    temperature.setText(data.temperature + "°C")
  }

  private def updateWeatherText(data: WeatherData): Unit = {
    weather.setText(data.weather)
  }

  private def updateLocationText(location: String): Unit = {
    myLocation.setText(location)
  }

  private def updateUpdatedAsOfText(): Unit = {
    val now = Calendar.getInstance()
    updatedAsOf.setText("Updated as of " +
      now.get(Calendar.HOUR_OF_DAY) + ":" +
      (if (now.get(Calendar.MINUTE) < 10) "0" + now.get(Calendar.MINUTE) else now.get(Calendar.MINUTE)) + ":" +
      (if (now.get(Calendar.SECOND) < 10) "0" + now.get(Calendar.SECOND) else now.get(Calendar.SECOND)))
  }

  private def updatePM25(data: AirData): Unit = {
    pm25.setText("PM2.5 = " + data.pm25)
  }

  private def updatePM10(data: AirData): Unit = {
    pm10.setText("PM10 = " + data.pm10)
  }

  private def updateCO(data: AirData): Unit = {
    co.setText("CO = " + data.co)
  }

  private def updateNO2(data: AirData): Unit = {
    no2.setText("NO2 = " + data.no2)
  }

  def setWeatherSource(weatherSource: WeatherSource): Unit = this.currentWeatherSource = weatherSource
}
