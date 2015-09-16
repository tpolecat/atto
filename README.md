# atto: everyday parsers

[![Join the chat at https://gitter.im/tpolecat/atto](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tpolecat/atto?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Travis CI](https://travis-ci.org/tpolecat/atto.svg?branch=master)](https://travis-ci.org/tpolecat/atto)

**atto** is a compact, pure-functional, incremental text parsing library for Scala (if you're looking for **binary** parsing, please turn your attention to [scodec](https://github.com/scodec/scodec)). The **atto** API is non-invasive (nothing to extend!) and uses sensible and familiar abstractions. **atto** parsers are a fun and principled tool for everyday parsing.

```scala
scala> int.sepBy1(spaceChar).parseOnly("1 20 300").option
res0: Option[scalaz.NonEmptyList[Int]] = Some(NonEmptyList(1, 20, 300))
```

Current version is **0.4.2** and is available for Scala 2.10 and 2.11 with **scalaz 7.1**.

### What's New?

Notable changes since 0.4.1 include:

- **atto** is now published on Sonatype, so the Bintray resolvers are no longer needed.
- New character parsers `whitespace`, `horizontalWhitespace`, `oneOf`, and `noneOf`.
- New text parsers `skipWhitespace` and `token` for dealing with tokens.
- New text parsers `bracket`, `parens`, `squareBrackets`, `braces`, `envelopes`, and `bananas` for dealing with bracketed text.
- Additional syntax for `.token`, `.parens`, `.sepBy*`, `.many*`, and `.skipMany*`.
- Constructions that combine multiple parsers such as `discard*` and `either` are now strict only in the initial parser, which makes it easier to write recursive parsers that would otherwise need to use `delay`.

In addition to the above there were some logistical changes and a bug fix in `optElem`. For details on all changes please see the [milestone](https://github.com/tpolecat/atto/issues?q=milestone%3A0.4.2+is%3Aclosed).

Many thanks to the generous folks who contributed to this release:

- Rúnar Óli Bjarnason
- Adelbert Chang
- Jonathan Ferguson
- Alberto Jácome

### Getting Started

Add **atto** as a dependency in your `build.sbt` file.

```scala
libraryDependencies += "org.tpolecat" %% "atto-core"  % "0.4.2"
```

if you are using [Spire](https://github.com/non/spire) and want parsers for unsigned integral types you can also add `atto-spire`. Experimental integration with [scalaz-stream](https://github.com/scalaz/scalaz-stream) is provided by `atto-stream` which can be added as above. This tiny library provides combinators to turn `Parser[A]` into `Process1[String, A]` with a few variations. There is a very basic example given [here](https://github.com/tpolecat/atto/blob/master/example/src/main/scala/atto/example/StreamExample.scala). 


### Why atto?

**atto** differs from stdlib parser combinators in a number of ways:

- You don't have to extend a trait or implement any methods.
- There is no tokenizer; the input type is always `Char`.
- Abstractions are better defined, which leads to simpler, more general code. `Parser` is a scalaz `Monad` for example, which gives us a lot of helpful operations for free.
- Parsers are *incremental* which means you can evaluate whether a prefix of your input is "ok so far." This can be helpful when working with streams or interactive UIs.

It's not a big deal to construct and use **atto** parsers; use them in any situation where you might otherwise reach for regular expressions or raw string manipulation.

Although **atto** is 50 times faster now than version 0.1, it's still not the fastest parsing lib on the block. If you're doing massive data processing you might look at a heavier library like Parboiled2, or even a hand-built parser like those used in the fastest JSON libs. But for "everyday" parsing where you have to turn user input into something useful, **atto** is a friendly little library to use.

### Documentation

Behold:

- A wee REPL [tutorial](http://tpolecat.github.io/2014/04/13/atto-tutorial.html).
- A variety of tasty [examples](https://github.com/tpolecat/atto/tree/master/example/src/main/scala/atto/example).
- Read the source! Perhaps start with the [parser definitions](https://github.com/tpolecat/atto/tree/master/core/src/main/scala/atto/parser).

### Contributors

The core of **atto** originated in Edward Kmett's Scala port of [Attoparsec](https://github.com/bos/attoparsec). This library is an elaboration maintained by @tpolecat with contributions from some [very helpful folks](https://github.com/tpolecat/atto/graphs/contributors). Feedback and suggestions are always welcome.

### License

**Attoparsec**, a Haskell library, is licensed under BSD-3 as specified [here](https://github.com/bos/attoparsec); the derivative work **atto** is provided under the MIT licence [here](LICENSE). Both licenses appear in project metadata.

