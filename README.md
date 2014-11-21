# atto: everyday parsers

**atto** is a compact, pure-functional, incremental text parsing library for Scala (if you're looking for **binary** parsing, please turn your attention to [scodec](https://github.com/scodec/scodec)). The **atto** API is non-invasive (nothing to extend!) and uses sensible and familiar abstractions. **atto** parsers are a fun and principled tool for everyday parsing.

```scala
scala> sepBy(int, spaceChar).parseOnly("1 20 300").option
res0: Option[List[Int]] = Some(List(1, 20, 300))
```

Current version is **0.4.1** and is available for Scala 2.10 and 2.11 with **scalaz 7.1**.

Changes since 0.4.0 are minor but merit a release:

- Internal representation was changed to match similar change in attoparsec, yielding a nice performance boost. Thanks to @pocketberserker for this contribution.
- Text parser `stringOf1` and combinators `cons`, `many1`, and `sepBy1` now compute a `NonEmptyList`. This is a **breaking change** that may require updating your code, but you can simply `.map(_.list)` to get back to the old behavior.

The last release supporting scalaz 7.0 is atto 0.3 ... we can back-port changes from the 0.4 series to 0.3 to keep them in parity for a while, but only if someone asks.

### Getting Started

Add **atto** as a dependency in your `build.sbt` file. The `atto-core` library is probably all you need, but if you are using [Spire](https://github.com/non/spire) and want parsers for unsigned integral types you can also add `atto-spire`.

```scala
resolvers += "tpolecat"  at "http://dl.bintray.com/tpolecat/maven"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "atto-core"  % "0.4.1", // Core parsers and combinators
  "org.tpolecat" %% "atto-spire" % "0.4.1"  // Optional, parsers for unsigned integral types
)
```

Experimental integration with [scalaz-stream](https://github.com/scalaz/scalaz-stream) is provided by `atto-stream` which can be added as above. This tiny library provides combinators to turn `Parser[A]` into `Process1[String, A]` with a few variations. There is a very basic example given [here](https://github.com/tpolecat/atto/blob/master/example/src/main/scala/atto/example/StreamExample.scala). 

### Documentation

Behold:

- A wee REPL [tutorial](http://tpolecat.github.io/2014/04/13/atto-tutorial.html). (The only change from the 0.1 version is the new import for Spire combinators). 
- A variety of tasty [examples](https://github.com/tpolecat/atto/tree/master/example/src/main/scala/atto/example).
- Read the source! Perhaps start with the [parser definitions](https://github.com/tpolecat/atto/tree/master/core/src/main/scala/atto/parser).

### Contributors

The core of **atto** originated in @ekmett's Scala port of [Attoparsec](https://github.com/bos/attoparsec). This library is an elaboration maintained by @tpolecat with contributions from some [very helpful folks](https://github.com/tpolecat/atto/graphs/contributors). Feedback (complaints especially) and suggestions are always welcome.

### License

**Attoparsec**, a Haskell library, is licensed under BSD-3 as specified [here](https://github.com/bos/attoparsec); the derivative work **atto** is provided under the MIT licence [here](LICENSE). Both licenses appear in project metadata.

