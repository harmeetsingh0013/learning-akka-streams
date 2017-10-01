package actors

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, IOResult, Supervision}
import akka.util.ByteString
import model.{Event, EventMarshalling}
import spray.json._

import scala.concurrent.Future

object SupervisionAkkaStream extends App with EventMarshalling {

  val spath = Paths.get("/home/harmeet/workspace/oculus-analytics/logs/storeserv_lagom-2017-09-26.1.log")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(spath)

  val dpath = Paths.get("/home/harmeet/akka-stream-json-2")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(dpath)

  val decider: Supervision.Decider = {
    case _: RuntimeException  => Supervision.Stop
    case _                    => Supervision.Resume
  }

  val composedFlow: Flow[ByteString, ByteString, NotUsed] = Framing.delimiter(ByteString("\n"), 10240)
    .map(_.decodeString("UTF8"))
    .map(Event.parsing)
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
    .filter(_.log == "DEBUG")
    .map { event =>
      ByteString(event.toJson.compactPrint)
    }

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
