atto - friendly parsing
====================

This is a Scala port of `attoparsec`, forked from Ed Kmett's original, updated, sliced up in the style of `scalaz` and
expanded with more combinators and parsers for base types.

Progress thus far:

   * Updated to Scala 2.10 and scalaz 7.0
   * Implementation is now trampolined (slower, but no more stack overflows)
   * Additional combinators and parsing options
   * Beginnings of a tutorial below, and [here](src/test/scala/atto/Example.scala)
   * [NTriples](http://www.w3.org/TR/rdf-testcases/#ntriples) parser example in progress [here](src/test/scala/atto/NTriples.scala)

Known issues:

   * It is possible to construct a parser that does not behave properly when given incremental input. This is under investigation.

Getting Started
---------------

You need `scalaz` and `atto` and you probably want `spire`. Here's how to get all three.

```scala

resolvers ++= Seq(
  "releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "tpolecat"  at "http://dl.bintray.com/tpolecat/maven"
)

libraryDependencies ++= Seq(
  "org.scalaz"     %% "scalaz-core" % "7.0.2",
  "org.spire-math" %% "spire"       % "0.6.0",
  "org.tpolecat"   %% "atto"        % "0.1"
)
```

There is a short **tutorial** over [here](https://github.com/tpolecat/tut/blob/master/out/Atto.md). Enjoy!

