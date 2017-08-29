package utils

/**
  * Created by radek on 01.06.17.
  * Object for for debugging/logging purposes
  */
object Call {

  val DEBUG_MODE = false

  def LOG(s: Any) = println("[LOG]: " + s)

  def DBG(s: Any) = if (DEBUG_MODE) println("[DEBUG]: " + s)

  def ERR(s: Any) = println("[ERR]: " + s)
}
