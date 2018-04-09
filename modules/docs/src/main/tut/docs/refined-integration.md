---
layout: docs
title: Refined integration
---

### {{page.title}}

Atto benefits from an integration with the excellent [refined](https://github.com/fthomas/refined)
library.

#### Installation

The refined integration lives in the `atto-refined` module:

```scala
libraryDependencies += "org.tpolecat" %% "atto-refined" % "{{site.attoVersion}}"
```

#### Usage

We'll need the usual imports as well as a specific `atto-refined` import:

```tut:silent
import atto._, Atto._, syntax.refined._
import eu.timepit.refined.numeric._
```

We can refine any `Parser[T]` with a predicate `P` to obtain a `Parser[T Refined P]`, for example
if we pick up our `int` parser and refine it to parse only positive integers:

```tut
val positiveInt = int.refined[Positive]
```

It will be able to parse positive integers:

```tut
positiveInt parseOnly "123"
```

And will fail on negative integers:

```tut
positiveInt parseOnly "-123"
```

Check out [the refined's library README](https://github.com/fthomas/refined#provided-predicates) to
see a list of all the provided predicates.
