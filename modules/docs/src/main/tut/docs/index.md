---
layout: docs
title: Getting Started
---

### {{page.title}}

The current version of **atto** is **0.5.2**. It is available for Scala 2.10, 2.11, and 2.12. To include it as a dependency in your project, add the following to your `build.sbt` file.

```scala
libraryDependencies += "org.tpolecat" %% "atto-core"  % "0.5.2"
```

It is recommended that you also add a compatibility layer for the FP library you are using (if any). If you wish to limp along with the Scala standard library there is a built-in compatibility layer and no extra dependency is needed.

```scala
libraryDependencies +=
  "org.tpolecat" %% "atto-compat-scalaz71" % "0.5.2" // for scalaz 7.1
  "org.tpolecat" %% "atto-compat-scalaz72" % "0.5.2" // for scalaz 7.2
  "org.tpolecat" %% "atto-compat-cats"     % "0.5.2" // for cats 0.9.0
```

These layers provide:

- **Instances** for typeclasses that are not used in **atto** but are useful for end users.
- **Shims** for typeclasses that *are* used in **atto**.
- **Modes** for return types that differ between libraries (`Xor` vs. `\/` for example.)

The **tl;dr** is that if you import the right compatibility layer everything should work as expected.

Finally, add the following imports. Fine-grained imports are supported but it's usually fine to just import everything.

```scala
import atto._, Atto._, compat.scalaz._ // or compat.cats._
                                       // or compat.stdlib._
```

Write you a parser.
