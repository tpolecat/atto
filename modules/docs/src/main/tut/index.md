---
layout: home
title:  "Home"
section: "home"
---

### Hello

**atto** is a compact, pure-functional, incremental text parsing library for Scala (if you're looking for **binary** parsing, please turn your attention to [scodec](https://github.com/scodec/scodec)). The **atto** API is non-invasive (nothing to extend!) and uses sensible and familiar abstractions. **atto** parsers are a fun and principled tool for everyday parsing.

```tut:invisible
import atto._, Atto._, atto.compat.scalaz._
```

```tut
int.sepBy1(spaceChar).parseOnly("1 20 300").option
```

### What's New?

The **0.5.x** series is a **breaking** change from 0.4.x, but for most users the impact will be minor. The important changes are:

- **atto** no longer depends on scalaz. Instead you can select a compatibility layer.
- `Parser` and `ParseResult` are now **invariant**, so you may need to add explicit type arguments in some cases.
- The Spire and scalaz-stream support libraries have been dropped for now. Please speak up if you need them.

### Why atto?

**atto** differs from stdlib parser combinators in a number of ways:

- You don't have to extend a trait or implement any methods.
- There is no tokenizer; the input type is always `Char`.
- Abstractions are better defined, which leads to simpler, more general code. `Parser` is a `Monad` for example, which gives us a lot of helpful operations for free.
- Parsers are *incremental* which means you can evaluate whether a prefix of your input is "ok so far." This can be helpful when working with streams or interactive UIs.

It's not a big deal to construct and use **atto** parsers; use them in any situation where you might otherwise reach for regular expressions or raw string manipulation.

Although **atto** is 50 times faster now than version 0.1, it's still not the fastest parsing lib on the block. If you have a need for speed and don't need incremental parsing you might checkout FastParse or Parboiled2. But for "everyday" parsing where you just have to turn user input into something useful, **atto** is a friendly little library to use.

### History and Contributors

The core of **atto** originated in Edward Kmett's Scala port of [Attoparsec](https://github.com/bos/attoparsec). This library is an elaboration maintained by @tpolecat with contributions from some [very helpful folks](https://github.com/tpolecat/atto/graphs/contributors). Feedback and suggestions are always welcome.

### License

**Attoparsec**, a Haskell library, is licensed under BSD-3 as specified [here](https://github.com/bos/attoparsec); the derivative work **atto** is provided under the MIT licence [here](LICENSE). Both licenses appear in project metadata.
