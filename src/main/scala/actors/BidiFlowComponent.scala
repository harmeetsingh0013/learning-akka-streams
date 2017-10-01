package actors

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing, JsonFraming}
import akka.util.ByteString
import model.Event
import spray.json._
import util.LogStreamProcessor

/**
  * A BidiFlow is a graph component with two open inputs and two open outputs.
  */
object BidiFlowComponent extends App {

 /* val inFlow: Flow[ByteString, Event, NotUsed] = if(args(0).toLowerCase() == "json") {
    JsonFraming.objectScanner(102400)
      .map(_.decodeString("UTF8").parseJson.convertTo[Event])
  } else {
    Framing.delimiter(ByteString("\n"), 10240)
      .map(_.decodeString("UTF8"))
      .map(Event.parsing)
  }

  val outFlow: Flow[Event, ByteString, NotUsed] = if(args(1).toLowerCase() == "json") {
    Flow[Event].map(event => ByteString(event.toJson.compactPrint))
  } else {
    Flow[Event].map { event =>
      ByteString(LogStreamProcessor)
    }
  }*/
}
