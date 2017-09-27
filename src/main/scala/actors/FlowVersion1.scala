package actors

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import model.Event
import model.EventMarshlling._
import spray.json._

import scala.concurrent.Future

object FlowVersion1 extends App {

  val spath = Paths.get("/home/harmeet/workspace/oculus-analytics/logs/storeserv_lagom-2017-09-26.1.log")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(spath)

  val dpath = Paths.get("/home/harmeet/akka-stream-json")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(dpath)

  val frame: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), 10240)
    .map(_.decodeString("UTF8"))

  val parse: Flow[String, Event, NotUsed] = Flow[String].map(Event.parsing)

  val filter: Flow[Event, Event, NotUsed] = Flow[Event].filter(_.log == "DEBUG")

  val serialize: Flow[Event, ByteString, NotUsed] = Flow[Event].map { event =>
    ByteString(event.toJson.compactPrint)
  }

  val composedFlow: Flow[ByteString, ByteString, NotUsed] = frame.via(parse)
    .via(filter)
    .via(serialize)

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(composedFlow)
    .toMat(sink) (Keep.right)

  implicit val system = ActorSystem("akka-stream")
  implicit val ec = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes Write")
    system.terminate()
  }
}
