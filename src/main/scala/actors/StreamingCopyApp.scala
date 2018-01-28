package actors

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, RunnableGraph, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

object StreamingCopyApp extends App {

  val spath = Paths.get("/home/harmeet/knolx.log")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(spath)

  val dpath = Paths.get("/home/harmeet/akka-stream-copy")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(dpath)

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)

  implicit val system = ActorSystem("akka-stream")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read")
    system.terminate()
  }
}
