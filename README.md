# atto - friendly parsing

#### Overview

**atto** is a compact, pure-functional incremental text parsing library for Scala (if you're looking for **binary** parsing, please turn your attention to [scodec](https://github.com/scodec/scodec)). The **atto** API is non-invasive (nothing to extend!) and uses sensible and familiar abstractions rather than an elaborate DSL. The intent is to make proper parsers trivial to construct and use anywhere your program receives text input.

```scala
scala> many(int <~ opt(spaceChar)).parseOnly("1 20 300").option
res0: Option[List[Int]] = Some(List(1, 20, 300))
```

The current version (0.1) runs on Scala 2.10 with scalaz 7.0.

#### Provenance

The core of **atto** originated in a port of Bryan O'Sullivan's **attoparsec** library into Scala, done by Edward Kmett.


#### Changes for 0.1 from Kmett's original port

   * Updated to Scala 2.10 and scalaz 7.0
   * Implementation is now trampolined (slower, but no more stack overflows)
   * Additional combinators and parsing options
   * New tut-based **tutorial** over [here](https://github.com/tpolecat/tut/blob/master/out/Atto.md). Enjoy!
   * A few examples in progress [here](example/src/main/scala/atto/example/)

#### Known issues:

   * It is possible to construct a parser that does not behave properly when given incremental input. This is under investigation.
   * No idea how fast or slow it is. Doesn't seem terrible but hard to say for sure.

## Getting Started

You need `scalaz` and `atto`, and you want `spire` if you intend to use the unsigned integral parsers. Here's how to get all three.

```scala

resolvers ++= "tpolecat"  at "http://dl.bintray.com/tpolecat/maven"

libraryDependencies ++= Seq(
  "org.scalaz"     %% "scalaz-core" % "7.0.2",
  "org.spire-math" %% "spire"       % "0.6.0",
  "org.tpolecat"   %% "atto"        % "0.1"
)
```

Continue with the **tutorial** over [here](https://github.com/tpolecat/tut/blob/master/out/Atto.md). Enjoy!

## Progress for 0.2

#### Changes thus far in 0.2

- New tests and combinators contributed by Rúnar Bjarnason.
- Split core, spire, and examples into their own projects.

#### TODO

- Many more tests.
- Track down the [rare] problem with incremental parsing.
- Do some benchmarking with simple grammars to see how we stack up.
- Scala 2.11 w/ scalaz 7.1
- Plug into **scalaz-stream** with possible transformations
  - `Parser[A] => Process1[String, ParseResult[A]]` continuous
  - `Parser[A] => Process1[String, A]` terminating with an exception on failed parse
  - `Parser[A] => Process1[String, String \/ A]` resetting on failure
  - `Parser[A] => Process1[String, Option[A]]` resetting on failure

