package com.twitter.finagle.examples.names

import com.twitter.logging.Logger
import com.twitter.util.Try
import java.io.{File, FileInputStream}
import opennlp.tools.namefind.{NameFinderME, TokenNameFinder, TokenNameFinderModel}
import opennlp.tools.sentdetect.{SentenceDetector, SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}

/**
 * A helper class that allows us to keep most of the gritty details about how
 * our models are deserialized out of the definition of
 * [[com.twitter.finagle.examples.names.NameRecognizer]].
 *
 * Note that for some languages we may not have model files for sentence
 * boundary detection or tokenization; in these cases we fall back to the
 * English-language models.
 */
class ModelLoader(baseDir: File) {
  private val log = Logger.get(getClass)

  protected def loadSentenceDetector(file: File): Try[SentenceDetector] = Try {
    val stream = new FileInputStream(file)
    val detector = new SentenceDetectorME(new SentenceModel(stream))
    stream.close()
    detector
  }

  protected def loadTokenizer(file: File): Try[Tokenizer] = Try {
    val stream = new FileInputStream(file)
    val tokenizer = new TokenizerME(new TokenizerModel(stream))
    stream.close()
    tokenizer
  }

  protected def loadNameFinder(file: File): Try[TokenNameFinder] = Try {
    val stream = new FileInputStream(file)
    val finder = new NameFinderME(new TokenNameFinderModel(stream))
    stream.close()
    finder
  }

  protected def defaultSentenceDetectorModel(lang: String): File = {
    val langModel = new File(baseDir, s"$lang-sent.bin")

    if (!langModel.exists || !langModel.isFile) {
      log.info(s"$langModel does not exist for language $lang; using English model.")
      new File(baseDir, "en-sent.bin")
    } else {
      langModel
    }
  }

  protected def defaultTokenizerModel(lang: String): File = {
    val langModel = new File(baseDir, s"$lang-token.bin")

    if (!langModel.exists || !langModel.isFile) {
      log.info(s"$langModel does not exist for language $lang; using English model.")
      new File(baseDir, "en-token.bin")
    } else {
      langModel
    }
  }

  protected def defaultPersonalNameModel(lang: String): File = {
    new File(baseDir, s"$lang-ner-person.bin")
  }

  protected def defaultLocationNameModel(lang: String): File = {
    new File(baseDir, s"$lang-ner-location.bin")
  }

  protected def defaultOrganizationNameModel(lang: String): File = {
    new File(baseDir, s"$lang-ner-organization.bin")
  }
}
