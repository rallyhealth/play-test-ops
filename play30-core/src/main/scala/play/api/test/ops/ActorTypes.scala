package play.api.test.ops

trait ActorTypes {
  type ActorSystem = org.apache.pekko.actor.ActorSystem
  type ByteString = org.apache.pekko.util.ByteString
  type Materializer = org.apache.pekko.stream.Materializer

  val ActorSystem = org.apache.pekko.actor.ActorSystem
  val Materializer = org.apache.pekko.stream.Materializer
  // Despite the package name this is from play-test
  val NoMaterializer = org.apache.pekko.stream.testkit.NoMaterializer
}

object ActorTypes extends ActorTypes