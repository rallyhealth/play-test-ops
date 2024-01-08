package play.api.test.ops

trait ActorTypes {
  type ActorSystem = akka.actor.ActorSystem
  type ByteString = akka.util.ByteString
  type Materializer = akka.stream.Materializer

  val ActorSystem = akka.actor.ActorSystem
  val Materializer = akka.stream.Materializer
  // Despite the package name this is from play-test
  val NoMaterializer = akka.stream.testkit.NoMaterializer
}

object ActorTypes extends ActorTypes