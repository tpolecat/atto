atto
====

Scala port of `attoparsec`, forked from kmett. Progress thus far:

   * Updated to Scala 2.10 and scalaz 7.0
   * Implementation is now trampolined (slower, but no more stack overflows)
   * Additional combinators and parsing options
   * Beginnings of a tutorial [here](src/test/scala/atto/Example.scala)
   * [NTriples](http://www.w3.org/TR/rdf-testcases/#ntriples) parser example in progress [here](src/test/scala/atto/NTriples.scala)

Known issues:

   * `WithFilter` can still cause stack overflows, so I am considering removing it; it's trivial to implement `filter` in terms of `flatMap`.
   * It is possible to construct a parser that does not behave properly when given incremental input. This is under investigation.

   