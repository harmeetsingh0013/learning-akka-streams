package actors

import java.nio.file.Paths

import scala.concurrent.ExecutionContext.Implicits.global
import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

object MultipleMaterializedValues extends App {

  val spath = Paths.get(
    "/home/harmeet/workspace/oculus-analytics/logs/storeserv_lagom-2017-09-26.1.log")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(spath)

  val dpath = Paths.get("/home/harmeet/akka-stream-copy")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(dpath)

  val graphLeft: RunnableGraph[Future[IOResult]] =
    source.toMat(sink)(Keep.left)
  val graphRight: RunnableGraph[Future[IOResult]] =
    source.toMat(sink)(Keep.right)
  val graphBoth: RunnableGraph[(Future[IOResult], Future[IOResult])] =
    source.toMat(sink)(Keep.both)
  val graphCustom: RunnableGraph[Future[Done]] = source.toMat(sink) { (l, r) =>
    Future.sequence(List(l, r)).map(_ => Done)
  }

  implicit val system = ActorSystem("akka-stream")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  graphBoth.run() match {
    case (l, r) =>
      l.foreach(le => println(s"Left: ${le.status}, ${le.count} bytes read"))
      r.foreach(ri => println(s"Right: ${ri.status}, ${ri.count} bytes read"))
  }
}
