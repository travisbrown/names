package com.twitter.finagle.examples.names

import com.twitter.algebird.Monoid

/**
 * Represents the result of running name recognition on a text.
 */
case class NameResult(persons: Seq[String], locations: Seq[String], organizations: Seq[String]) {
  lazy val personCounts: Map[String, Int] = countOccurrences(persons)
  lazy val locationCounts: Map[String, Int] = countOccurrences(locations)
  lazy val organizationCounts: Map[String, Int] = countOccurrences(organizations)

  protected def countOccurrences(names: Seq[String]): Map[String, Int] = {
    names.groupBy(identity) map {
      case (name, occurrences) => name -> occurrences.size
    }
  }
}

object NameResult {
  /**
   * We often want to combine partial results as we process a body of text.
   * Defining a [[com.twitter.algebird.Monoid]] here allows us to take advantage
   * of the abstractions provider by Algebird.
   */
  implicit object nameResultMonoid extends Monoid[NameResult] {
    val zero: NameResult = NameResult(Seq.empty, Seq.empty, Seq.empty)

    def plus(r1: NameResult, r2: NameResult): NameResult = {
      NameResult(
        r1.persons ++ r2.persons,
        r1.locations ++ r2.locations,
        r1.organizations ++ r2.organizations)
    }
  }
}
