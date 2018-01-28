package actors

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}

object GraphDSLCloseGraph extends App {

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val in = Source(1 to 5)
      val out = Sink.foreach[Int](println)
      val f1 = Flow[Int].map(_ * 2)
      val f2 = Flow[Int].map(_ * 1)
      val f3 = Flow[Int].map(_ * 2)
      val f4 = Flow[Int].map(_ + 1)

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f4 ~> out
      bcast ~> f3 ~> merge
      ClosedShape
  })

  implicit val system = ActorSystem("akka-stream")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  graph.run(materializer)
}
