package com.twitter.finagle.examples.names

import com.twitter.util.Try
import java.io.File
import opennlp.tools.namefind.TokenNameFinder
import opennlp.tools.sentdetect.SentenceDetector
import opennlp.tools.tokenize.Tokenizer
import opennlp.tools.util.Span

/**
 * Processes text to extract names of people, places, and organizations. Note
 * that this class and its underlining OpenNLP processing tools are not
 * thread-safe.
 */
class NameRecognizer(
  val lang: String,
  sentenceDetector: SentenceDetector,
  tokenizer: Tokenizer,
  personalNameFinder: TokenNameFinder,
  locationNameFinder: TokenNameFinder,
  organizationNameFinder: TokenNameFinder) {

  /**
   * The default interface to the recognizer; finds names in a document and then
   * clears adaptive data that was gathered during the processing.
   */
  def findNames(document: String): NameResult = {
    val sentences = sentenceDetector.sentDetect(document)
    val tokenized = sentences map { sentence => tokenizer.tokenize(sentence) }
    val results = tokenized map { tokens => findNamesInTokens(tokens) }
    val result = NameResult.sum(results)

    clearAfterDocument()

    result
  }

  /**
   * In some cases the user may wish to process a single sentence out of
   * context and clear adaptive data immediately.
   */
  def findNamesInSentence(sentence: String): NameResult = {
    val tokenized = tokenizer.tokenize(sentence)
    val result = findNamesInTokens(tokenized)

    clearAfterDocument()

    result
  }

  protected def clearAfterDocument(): Unit = {
    personalNameFinder.clearAdaptiveData()
    locationNameFinder.clearAdaptiveData()
    organizationNameFinder.clearAdaptiveData()
  }

  protected def findNamesInTokens(tokens: Array[String]): NameResult = {
    val personalNames = identifyNames(personalNameFinder, tokens)
    val locationNames = identifyNames(locationNameFinder, tokens)
    val organizationNames = identifyNames(organizationNameFinder, tokens)

    NameResult(personalNames, locationNames, organizationNames)
  }

  protected def identifyNames(finder: TokenNameFinder, tokens: Array[String]): Seq[String] = {
    Span.spansToStrings(finder.find(tokens), tokens)
  }
}

object NameRecognizer extends ModelLoader(new File("models")) {
  /**
   * Creates a recognizer given a language identifier and (optionally) paths to
   * the OpenNLP models to use.
   */
  def create(
    lang: String,
    sentenceDetectorModel: File = null,
    tokenizerModel: File = null,
    personalNameModel: File = null,
    locationNameModel: File = null,
    organizationNameModel: File = null): Try[NameRecognizer] = {

    val sdModelFile = Option(sentenceDetectorModel).getOrElse(defaultSentenceDetectorModel(lang))
    val tkModelFile = Option(tokenizerModel).getOrElse(defaultTokenizerModel(lang))
    val pnModelFile = Option(personalNameModel).getOrElse(defaultPersonalNameModel(lang))
    val lnModelFile = Option(locationNameModel).getOrElse(defaultLocationNameModel(lang))
    val onModelFile = Option(organizationNameModel).getOrElse(defaultOrganizationNameModel(lang))

    for {
      sentenceDetector       <- loadSentenceDetector(sdModelFile)
      tokenizer              <- loadTokenizer(tkModelFile)
      personalNameFinder     <- loadNameFinder(pnModelFile)
      locationNameFinder     <- loadNameFinder(lnModelFile)
      organizationNameFinder <- loadNameFinder(onModelFile)
    } yield {
      new NameRecognizer(
        lang,
        sentenceDetector,
        tokenizer,
        personalNameFinder,
        locationNameFinder,
        organizationNameFinder)
    }
  }
}
