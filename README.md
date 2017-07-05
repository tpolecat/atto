# atto: everyday parsers

[![Join the chat at https://gitter.im/tpolecat/atto](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tpolecat/atto?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Travis CI](https://travis-ci.org/tpolecat/atto.svg?branch=master)](https://travis-ci.org/tpolecat/atto)

**atto** is a compact, pure-functional, incremental text parsing library for Scala (if you're looking for **binary** parsing, please turn your attention to [scodec](https://github.com/scodec/scodec)). The **atto** API is non-invasive (nothing to extend!) and uses sensible and familiar abstractions. **atto** parsers are a fun and principled tool for everyday parsing.

```scala
scala> int.sepBy1(spaceChar).parseOnly("1 20 300").option
res0: Option[scalaz.NonEmptyList[Int]] = Some(NonEmptyList(1, 20, 300))
```

Current version is **0.6.0** and is available for Scala 2.10 (JVM only), 2.11, and 2.12 (JVM and Scala-JS) with **cats 0.9**.

### What's New?

**atto** has standardized on [Cats](https://github.com/typelevel/cats). The 0.5.x compatibility layers have been removed.

### Getting Started

- Add **atto** as a dependency in your `build.sbt` file.

```scala
libraryDependencies += "org.tpolecat" %% "atto-core"  % "0.6.0"
```

- Profit.

### Why atto?

**atto** differs from stdlib parser combinators in a number of ways:

- You don't have to extend a trait or implement any methods.
- There is no tokenizer; the input type is always `Char`.
- Abstractions are better defined, which leads to simpler, more general code. `Parser` is a scalaz `Monad` for example, which gives us a lot of helpful operations for free.
- Parsers are *incremental* which means you can evaluate whether a prefix of your input is "ok so far." This can be helpful when working with streams or interactive UIs.

It's not a big deal to construct and use **atto** parsers; use them in any situation where you might otherwise reach for regular expressions or raw string manipulation.

Although **atto** is 50 times faster now than version 0.1, it's still not the fastest parsing lib on the block. If you're doing massive data processing you might look at a heavier library like Parboiled2, or even a hand-built parser like those used in the fastest JSON libs. But for "everyday" parsing where you have to turn user input into something useful, **atto** is a friendly little library to use.

### Support

- Chat it up on [Gitter](https://gitter.im/tpolecat/atto).
- Check the new documentation [microsite](https://tpolecat.github.com/atto/).

### Contributors

The core of **atto** originated in Edward Kmett's Scala port of [Attoparsec](https://github.com/bos/attoparsec). This library is an elaboration maintained by @tpolecat with contributions from some [very helpful folks](https://github.com/tpolecat/atto/graphs/contributors). Feedback and suggestions are always welcome.

### License

**Attoparsec**, a Haskell library, is licensed under BSD-3 as specified [here](https://github.com/bos/attoparsec); the derivative work **atto** is provided under the MIT licence [here](LICENSE). Both licenses appear in project metadata.
