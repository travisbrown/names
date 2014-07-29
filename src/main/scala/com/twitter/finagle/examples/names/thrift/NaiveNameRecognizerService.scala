package com.twitter.finagle.examples.names.thrift

import com.twitter.finagle.examples.names.NameRecognizer
import com.twitter.util.{Future, Return, Throw, Try}

/**
 * A naive service implementation that implements the trait defined by Scrooge.
 *
 * There are two serious problems with this implementation! It is not
 * thread-safe (we're using a single instance of NameRecognizer, which maintains
 * internal state during processing) and it blocks a Finagle thread.
 */
class NaiveNameRecognizerService(recognizer: NameRecognizer) extends NameRecognizerService[Future] {
  def findNames(document: String): Future[NameRecognizerResult] = Future {
    val result = recognizer.findNames(document)

    new NameRecognizerResult {
      val persons = result.persons
      val locations = result.locations
      val organizations = result.organizations
    }
  }
}

object NaiveNameRecognizerService {
  /**
   * A simple constructor that synchronously creates a service, encapsulating
   * errors in a Try.
   */
  def create(lang: String): Try[NameRecognizerService[Future]] =
    NameRecognizer.create(lang) map { recognizer =>
      new NaiveNameRecognizerService(recognizer)
    }
}
