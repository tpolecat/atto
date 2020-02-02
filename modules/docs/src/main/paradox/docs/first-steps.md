## First Steps

In this chapter we will learn the basics of using primitive parsers, and combining them to build larger parsers.

But first we need to import some stuff. Fine-grained imports are supported but it’s usually fine to just import everything. We also need cats implicits for applicative syntax below.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #imports }

Rock on, let's parse an integer!

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #abc }

This result means we successfully parsed an `Int` and have the text `"abc"` left over. We'll talk more about this momentarily. But let's back up. What's this `int` thing?

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #int }

A `Parser[A]` is a computation that consumes characters and produces a value of type `A`. In this case `Int`. Let's look at another predefined parser that matches only characters where `isLetter` is true.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #letter }


We can ask a parser to parse a string, and we get back a `ParseResult[A]`. The `Done` constructor shows the remaining input (if any) and the answer.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #letter-parse-1 }

The `Failure` constructor shows us the remaining input, the parsing stack (ignore this for now), and a description of the failiure.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #letter-parse-2 }

The `Partial` constructor indicates that the parser has neither succeeded nor failed; more input is required before we will know. We can `feed` more data to continue parsing. Our parsers thus support *incremental parsing* which allows us to parse directly from a stream, for example.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #letter-parse-3 }

The `many` combinator turns a `Parser[A]` into a `Parser[List[A]]`.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #many-letter-parse-1 }

There may be more letters coming, so we can say we're `done` to indicate that there is no more input.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #many-letter-parse-2 }

`Parser` is a functor, so you can `map` the result and turn it int something else.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #many-letter-map }

The `~` combinator turns `Parser[A], Parser[B]` into `Parser[(A,B)]`

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #twiddle-1 }

Destructuring the pair in `map` is a pain, and it gets worse with nested pairs.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #twiddle-2 }

But have no fear, `Parser` is an *applicative* functor.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #applicative }

In fact, it's a monad. This allows the result of one parser to influence the behavior of subsequent parsers. Here we build a parser that parses an integer followed by an arbitrary string of that length.

@@snip [first-steps.scala](/modules/docs/src/main/scala/first-steps.scala) { #monad }

In the next chapter we will build up parsers for larger structures.
