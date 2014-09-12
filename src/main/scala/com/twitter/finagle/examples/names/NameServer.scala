import com.twitter.finagle.Thrift
import com.twitter.finagle.examples.names.thrift._
import com.twitter.server.TwitterServer
import com.twitter.util.Await

object NamesServer extends TwitterServer {
  val service = SafeNameRecognizerService.create("en", 4)

  def main() {
    val server = Thrift.serveIface("localhost:9090", Await.result(service))

    onExit {
      server.close()
    }
    
    Await.ready(server)
  }
}
