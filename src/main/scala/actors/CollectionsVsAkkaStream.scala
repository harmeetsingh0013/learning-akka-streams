package actors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

object CollectionsVsAkkaStream extends App {

  /* Simple collection working as in batches */
  println("Simple collection working as in batches")
  val collection = (1 to 10)
    .map { x =>
      println(s"pre-map: $x"); x
    }
    .map(_ * 3)
    .map { x =>
      println(s"post-map: $x"); x
    }
    .filter(_ % 2 == 0)
    .foreach(x => println(s"Done: $x"))

  /* collection stream working as in per elements */
  println("Collection stream working as in per elements")
  val collectionStream = (1 to 10).toStream
    .map { x =>
      println(s"pre-map: $x"); x
    }
    .map(_ * 3)
    .map { x =>
      println(s"post-map: $x"); x
    }
    .filter(_ % 2 == 0)
    .foreach(x => println(s"Done: $x"))

  /* Akka stream working as in per elements*/
  implicit val system = ActorSystem("akka-stream")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  println("Akka stream working as in per elements")
  val akkaStream = Source(1 to 10)
    .map { x =>
      println(s"pre-map: $x"); x
    }
    .map(_ * 3)
    .async
    .map { x =>
      println(s"post-map: $x"); x
    }
    .filter(_ % 2 == 0)
    .runForeach(x => println(s"Done: $x"))
}
