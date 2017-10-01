package actors

import java.nio.file.Paths

import actors.FlowVersion1.{runnableGraph, system}
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, RunnableGraph, Source}
import akka.stream._
import akka.util.ByteString
import model.{Event, EventMarshalling}
import spray.json._

import scala.concurrent.Future

object GraphDSLBroadcasting extends App with EventMarshalling {

  val spath = Paths.get("/home/harmeet/workspace/oculus-analytics/logs/storeserv_lagom-2017-09-26.1.log")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(spath)

  val composeGraph: Flow[ByteString, Event, NotUsed] = Framing.delimiter(ByteString("\n"), 10240)
    .map(_.decodeString("UTF8"))
    .map(Event.parsing)

  type FlowLike = Graph[FlowShape[Event, ByteString], NotUsed]
  val jsonOutFlow = Flow[Event].map(event => ByteString(event.toJson.toString()))

  val processGraph: Flow[Event, ByteString, NotUsed] = Flow.fromGraph {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val bcast: UniformFanOutShape[Event, Event] = builder.add(Broadcast[Event](4))
      val js: FlowShape[Event, ByteString] = builder.add(jsonOutFlow)

      val info = Flow[Event].filter(_.log == "INFO")
      val debug = Flow[Event].filter(_.log == "DEBUG")
      val error = Flow[Event].filter(_.log == "ERROR")

      bcast ~> js.in
      bcast ~> info     ~> jsonOutFlow ~> logFileSink("info")
      bcast ~> debug    ~> jsonOutFlow ~> logFileSink("debug")
      bcast ~> error    ~> jsonOutFlow ~> logFileSink("error")

      FlowShape(bcast.in, js.out)
    }
  }

  val runnableGraph: RunnableGraph[Future[IOResult]] = source
    .via(composeGraph)
    .via(processGraph)
    .to(logFileSink("all"))

  implicit val system = ActorSystem("akka-stream")
  implicit val ec = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes Write")
    system.terminate()
  }

  private def logFileSink(`type`: String) = {
    val dpath = Paths.get(s"/home/harmeet/akka-stream-copy-${`type`}")
    FileIO.toPath(dpath)
  }
}
