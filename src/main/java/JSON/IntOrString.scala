package JSON

/**
  * Created by radek on 01.06.17.
  * Variant type for JSON parsing purposes
  */
trait IntOrString

object IntOrString {
  def pack(x: Any): IntOrString = x match {
    case s: String => Jfield(s)
    case i: Int => Jid(i)
    case _ => throw new RuntimeException("Neither String nor Int")
  }
}
