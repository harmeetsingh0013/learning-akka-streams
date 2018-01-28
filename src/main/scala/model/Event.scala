package model

import spray.json.DefaultJsonProtocol

case class Event(
    t1: String,
    t2: String,
    thread: String,
    log: String,
    description: String
)

object Event {

  def parsing(string: String) = {
    val event = string.split("\\s+", 5)
    Event(t1 = event(0),
          t2 = event(1),
          thread = event(2),
          log = event(3),
          description = event(4))
  }
}

trait EventMarshalling extends DefaultJsonProtocol {
  implicit val formatter = jsonFormat5(Event.apply)

}
