atto
====

Scala port of `attoparsec`, forked from kmett. Progress thus far:

   * Updated to Scala 2.10 and scalaz 7.0
   * Implementation is now trampolined (slower, but no more stack overflows)
   * Additional combinators and parsing options
   * Beginnings of a tutorial [here](src/test/scala/atto/Example.scala)
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
An easy way to get started is to add these imports. The examples below assume you have done this.

```scala
initialCommands :=
  """import scalaz._
     import Scalaz._
     import atto._
     import Atto._"""

```

Let's parse an integer!

```scala
scala> int parseOnly "123abc"
res0: atto.ParseResult[Int] = Done(abc,123)
```

