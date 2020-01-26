## A Larger Example

Our goal for this chapter is to parse the following log data into structured values. This is taken from a nice tutorial over at [FP Complete](https://www.fpcomplete.com/school/text-manipulation/attoparsec).

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #logdata }

We'll use the same imports as before.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #imports }

This data contains IP addresses, which in turn contain unsigned bytes. So our first order of business is figuring out how to parse these.

### Parsing Unsigned Bytes and IP Addresses

IP addresses contain unsigned bytes, which we don't have in Scala. So the first thing we'll do is create a data type wrapping a signed byte, and then write a parser for it.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ubyte }

It works!

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ubyte-parse }

We can now define our `IP` data type and a parser for it. As a first pass we can parse an IP address in the form 128.42.30.1 by using the `ubyte` and `char` parsers directly, in a `for` comprehension.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip }

It works!

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-parse }

The `<~` and `~>` combinators combine two parsers sequentially, discarding the value produced by
the parser on the `~` side. We can factor out the dot and use `<~` to simplify our comprehension a bit.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-2 }

And it still works.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-parse-2 }

We can name our parser, which provides slightly more enlightening failure messages

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-named }

Thus.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-named-parse }

Since nothing that occurs on the right-hand side of our <- appears on the left-hand side, we
don't actually need a monad; we can use applicative syntax here.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-4 }

And it still works.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-4-parse }

We might prefer to get some information about failure, so `either` is an, um, option.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #ip-4-parse-either }

Ok, so we can parse IP addresses now. Let's move on to the log.

### Parsing Log Entries

Here are some data types for the log data above.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #data-types }

There's no built-in parser for fixed-width ints, so we can just make one. We parse some number of digits and parse them as an `Int`, handling the case where the value is too large by flatmapping to `ok` or `err`.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #fixed }

Now we have what we need to put the log parser together.

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #log }

It works!

@@snip [next-steps.scala](/modules/docs/src/main/scala/next-steps.scala) { #log-parse }
