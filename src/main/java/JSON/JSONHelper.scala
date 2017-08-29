package JSON

/**
  * Created by radek on 01.06.17.
  * Object created for managing parsed JSONs to scala's Map
  */
object JSONHelper {

  val emptyDefault = "-"

  /**
    * Extracts smartly strings from Option Any
    * @param p Maybe valid data
    * @return `emptyDefault` on failure, valid data on success
    */
  private def fix(p: Option[Any]): String = p match {
    case None => emptyDefault
    case Some(s: String) => s
    case Some(s: Int) => s toString
    case Some(s: Double) => s toString
    case _ => emptyDefault
  }

  /**
    * Extracts value from certain field in JSON map
    * @param field List representing path to desired value.
    *              May contain either JString (name of next field)
    *              or JInt (index of JSON list if met)
    * @param map Map to begin the search
    * @return Valid data on success or emptyDefault on fail
    */
  def read(field: List[IntOrString])(map: Map[String, Any]): String = {
    field match {
      case Nil => emptyDefault
      case Jfield(x) :: Nil => fix(map.get(x))
      case Jfield(s) :: Jid(id) :: tail => map.get(s) match {
        case Some(l: List[Any]) => if (l.size <= id) emptyDefault
        else l(id) match {
          case m: Map[String, Any] => read(tail)(m)
          case _ => fix(Some(l(id)))
        }
        case _ => emptyDefault
      }
      case Jfield(x) :: tail => map.get(x) match {
        case Some(map2: Map[String, Any]) => read(tail)(map2)
        case _ => emptyDefault
      }
      case _ => emptyDefault
    }
  }
}
