---
layout: docs
title: Basic Parsers
---

### {{page.title}}

In this chapter we will learn the basics of using primitive parsers, and combining them to build larger parsers.

But first we need to import some stuff. Fine-grained imports are supported but it’s usually fine to just import everything. We're using the compatibility layer for **scalaz** here but you could just as well use the one for Cats or stdlib by importing `compat.cats._` or `compat.stdlib._` respectively.

```tut:silent
import atto._, Atto._
```

Rock on, let's parse an integer!

```tut
int parseOnly "123abc"
```

This result means we successfully parsed an `Int` and have the text `"abc"` left over. We'll talk more about this momentarily. But let's back up. What's this `int` thing?

```tut
int
```

A `Parser[A]` is a computation that consumes characters and produces a value of type `A`. In this case `Int`. Let's look at another predefined parser that matches only characters where `isLetter` is true.

```tut
letter
```

We can ask a parser to parse a string, and we get back a `ParseResult[A]`. The `Done` constructor shows the remaining input (if any) and the answer.

```tut
letter.parse("x")
letter.parse("xyz")
```

The `Failure` constructor shows us the remaining input, the parsing stack (ignore this for now), and a description of the failiure.

```tut
letter.parse("1")
```

The `Partial` constructor indicates that the parser has neither succeeded nor failed; more input is required before we will know. We can `feed` more data to continue parsing. Our parsers thus support *incremental parsing* which allows us to parse directly from a stream, for example.

```tut
letter.parse("")
letter.parse("").feed("abc")
```

The `many` combinator turns a `Parser[A]` into a `Parser[List[A]]`.

```tut
many(letter).parse("abc")
many(letter).parse("abc").feed("def")
```

There may be more letters coming, so we can say we're `done` to indicate that there is no more input.

```tut
many(letter).parse("abc").feed("def").done
```

`Parser` is a functor, so you can `map` the result and turn it int something else.

```tut
many(letter).map(_.mkString).parse("abc").feed("def").done
```

The `~` combinator turns `Parser[A], Parser[B]` into `Parser[(A,B)]`

```tut
letter ~ digit
(letter ~ digit).parse("a1")
(many(letter) ~ many(digit)).parse("aaa")
(many(letter) ~ many(digit)).parse("aaa").feed("bcd123").done
(many(letter) ~ many(digit)).map { case (a, b) => a ++ b } .parse("aaa").feed("bcd123").done
```

Destructuring the pair in `map` is a pain, and it gets worse with nested pairs.

```tut
(letter ~ int ~ digit ~ byte)
```

But have no fear, `Parser` is an *applicative* functor.

```tut
(many(letter) |@| many(digit))(_ ++ _).parse("aaa").feed("bcd123").done
```

In fact, it's a monad. This allows the result of one parser to influence the behavior of subsequent parsers. Here we build a parser that parses an integer followed by an arbitrary string of that length.

```tut
val p = for { n <- int; c <- take(n) } yield c
p.parse("3abcdef")
p.parse("4abcdef")
```

In the next chapter we will build up parsers for larger structures.
