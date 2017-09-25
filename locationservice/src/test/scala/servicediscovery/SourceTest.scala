package servicediscovery

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.scalatest.FunSuite

class SourceTest extends FunSuite {

  test("iterator source") {
    implicit val actorSystem = ActorSystem("S")
    implicit val mat = ActorMaterializer()

    val source = Source(1 to 10)
    source.runForeach(i ⇒ println(i))
  }

}
