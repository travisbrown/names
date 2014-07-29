Named-entity recognition with Finagle
=====================================

This project is a demonstration of how you can use Finagle to build a service
that will identify names of people, places, and organizations in any text you
throw at it. It's currently a work in progress, and is not intended for public
use.

Quick start
-----------

You'll need to download the OpenNLP model files before you can run the project
tests or examples:

```
./download-models.sh
```

Next you can run `./sbt console` from the project root. This will use
[Scrooge][1] to generate our Thrift service and client traits, and then it'll
compile them along with the rest of our code and start a Scala console. Paste
the following lines to start a server running locally on port 9090:

``` scala
import com.twitter.finagle.Thrift
import com.twitter.finagle.examples.names.thrift._
import com.twitter.util.{Future, Return, Throw}

SafeNameRecognizerService.create("en", 4) onSuccess { service =>
  Thrift.serveIface("localhost:9090", service)
  println("Server started successfully")
} onFailure { exc =>
  println("Could not start the server: " + exc)
}
```

Now you can create a client to speak to the server:

``` scala
val client =
  Thrift.newIface[NameRecognizerService.FutureIface]("localhost:9090")

val doc = """
An anomaly which often struck me in the character of my friend Sherlock Holmes
was that, although in his methods of thought he was the neatest and most
methodical of mankind, and although also he affected a certain quiet primness of
dress, he was none the less in his personal habits one of the most untidy men
that ever drove a fellow-lodger to distraction. Not that I am in the least
conventional in that respect myself. The rough-and-tumble work in Afghanistan,
coming on the top of a natural Bohemianism of disposition, has made me rather
more lax than befits a medical man.
"""

client.findNames(doc) onSuccess { response =>
  println("People: " + response.persons.mkString(", "))
  println("Places: " + response.locations.mkString(", "))
}
```

This will print the following:

```
People: Sherlock Holmes
Places: Afghanistan
```

As we'd expect.

[1]: http://twitter.github.io/scrooge/
