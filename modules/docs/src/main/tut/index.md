---
layout: home
title:  "Home"
section: "home"
---

# atto

[![Join the chat at https://gitter.im/tpolecat/atto](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tpolecat/atto?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Travis CI](https://travis-ci.org/tpolecat/atto.svg?branch=master)](https://travis-ci.org/tpolecat/atto)

**atto** is a compact, pure-functional, incremental text parsing library for Scala. The API is non-invasive (nothing to extend!) and uses sensible and familiar abstractions. **atto** parsers are a fun and principled tool for everyday parsing.

```tut
import atto._, Atto._
int.sepBy(spaceChar).parseOnly("1 20 300").option
```

The current version is **{{site.attoVersion}}** for **Scala {{site.scalaVersions}}** and **Scala-JS** (2.11+) with **[cats](http://typelevel.org/cats/) {{site.catsVersion}}**. Add it to your `build.sbt` thus:

```scala
libraryDependencies += "org.tpolecat" %% "atto-core"    % "{{site.attoVersion}}"
libraryDependencies += "org.tpolecat" %% "atto-refined" % "{{site.attoVersion}}"
```

More information on `atto-refined` in [the dedicated guide](https://tpolecat.github.com/atto/docs/refined-integration.html)
which provides an integration with **[refined](https://github.com/fthomas/refined) {{site.refinedVersion}}**.

## Documentation and Support

- Chat it up on [Gitter](https://gitter.im/tpolecat/atto).
- Check the [tutorial](https://tpolecat.github.com/atto/docs/first-steps.html).
- Check the [Scaladoc](https://www.javadoc.io/doc/org.tpolecat/atto-core_2.12/{{site.attoVersion}}).

## Why atto?

**atto** differs from stdlib parser combinators in a number of ways:

- You don't have to extend a trait or implement any methods.
- There is no tokenizer; the input type is always `Char`.
- Abstractions are better defined, which leads to simpler, more general code. `Parser` is a cats `Monad` for example, which gives us a lot of helpful operations for free.
- Parsers are *incremental* which means you can evaluate whether a prefix of your input is "ok so far." This can be helpful when working with streams or interactive UIs.

It's not a big deal to construct and use **atto** parsers; use them in any situation where you might otherwise reach for regular expressions or raw string manipulation.

Although **atto** is 50 times faster now than version 0.1, it's still not the fastest parsing lib on the block. If you're doing massive data processing you might look at a library like [Fastparse](http://www.lihaoyi.com/fastparse/), or even a hand-built parser like those used in the fastest JSON libs. But for "everyday" parsing where you have to turn user input into something useful, **atto** is a friendly little library to use.

## Contributors

The core of **atto** originated in Edward Kmett's Scala port of [Attoparsec](https://github.com/bos/attoparsec). This library is an elaboration maintained by @tpolecat with contributions from some [very helpful folks](https://github.com/tpolecat/atto/graphs/contributors). Feedback and suggestions are always welcome.

## License

**Attoparsec**, a Haskell library, is licensed under BSD-3 as specified [here](https://github.com/bos/attoparsec); the derivative work **atto** is provided under the MIT licence [here](LICENSE). Both licenses appear in project metadata.
