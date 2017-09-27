package model

import spray.json.DefaultJsonProtocol

case class Event (
                 thread: String,
                 log: String,
                 description: String
                 )

object Event {

  def parsing(string: String) = {
    val event = string.split("\\s+", 5)
    Event(thread = event(2), log = event(3), description=event(4))
  }
}

object EventMarshlling extends DefaultJsonProtocol {
  implicit val formatter = jsonFormat3(Event.apply)

}