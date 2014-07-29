namespace java com.twitter.finagle.examples.names.thrift

struct NameRecognizerResult {
  1: list<string> persons;
  2: list<string> locations;
  3: list<string> organizations;
}

exception NameRecognizerException {
  1: string description;
}

service NameRecognizerService {
  NameRecognizerResult findNames(1: string document) throws(1: NameRecognizerException ex)
}
