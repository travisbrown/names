package com.twitter.finagle.examples.names.thrift

import com.twitter.concurrent.AsyncQueue
import com.twitter.finagle.examples.names.NameRecognizer
import com.twitter.util.{Future, FuturePool, NonFatal}
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, Executors}

/**
 * A simple service implementation that implements the trait defined by Scrooge.
 *
 * Each service owns a queue of recognizers that serves as a simple object pool.
 * Each request grabs a recognizer from the queue and returns it when processing
 * is finished. We also use a future pool to avoid blocking a Finagle thread.
 */
class SafeNameRecognizerService(recognizers: BlockingQueue[NameRecognizer], futurePool: FuturePool)
  extends NameRecognizerService[Future] {
  def findNames(document: String): Future[NameRecognizerResult] = futurePool {
    val recognizer = recognizers.take()

    try {
      val result = recognizer.findNames(document)

      new NameRecognizerResult {
        val persons = result.persons
        val locations = result.locations
        val organizations = result.organizations
      }
    } finally {
      recognizers.offer(recognizer)      
    }
  } rescue {
    case exception => Future.exception(
      NameRecognizerException("Error while processing document: " + exception.getMessage))
  }
}

object SafeNameRecognizerService {
  /**
   * An asynchronous constructor that creates a future pool, uses it to
   * initialize a pool of recognizers, and then creates a service with both
   * pools.
   */
  def create(lang: String, poolSize: Int): Future[NameRecognizerService[Future]] = {
    val futurePool = FuturePool(Executors.newFixedThreadPool(poolSize))

    val recognizerFutures = Seq.fill(poolSize) {
      futurePool {
        Future.const(NameRecognizer.create(lang))
      }.flatten
    }

    Future.collect(recognizerFutures).map { recognizers => 
      val queue = new ArrayBlockingQueue[NameRecognizer](poolSize)

      recognizers foreach { recognizer => queue.offer(recognizer) }

      new SafeNameRecognizerService(queue, futurePool)
    }
  }
}
