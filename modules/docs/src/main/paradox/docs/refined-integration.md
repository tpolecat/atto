
## Refined Integration

Atto benefits from an integration with the excellent [refined](https://github.com/fthomas/refined)
library.

#### Installation

The refined integration lives in the `atto-refined` module:

@@dependency[sbt,Maven,Gradle] {
  group="$org$"
  artifact="$refined-dep$"
  version="$version$"
}

#### Usage

We'll need the usual imports as well as a specific `atto-refined` import:

@@snip [refined-integration.scala](/modules/docs/src/main/scala/refined-integration.scala) { #imports }

We can refine any `Parser[T]` with a predicate `P` to obtain a `Parser[T Refined P]`, for example
if we pick up our `int` parser and refine it to parse only positive integers:

@@snip [refined-integration.scala](/modules/docs/src/main/scala/refined-integration.scala) { #positiveInt }

It will be able to parse positive integers:

@@snip [refined-integration.scala](/modules/docs/src/main/scala/refined-integration.scala) { #positiveInt-parse-1 }

And will fail on negative integers:

@@snip [refined-integration.scala](/modules/docs/src/main/scala/refined-integration.scala) { #positiveInt-parse-2 }


Check out [the refined's library README](https://github.com/fthomas/refined#provided-predicates) to
see a list of all the provided predicates.
