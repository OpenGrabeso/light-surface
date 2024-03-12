#### Light Surface

Fork of [Airframe Surface](https://github.com/wvlet/airframe/tree/main/airframe-surface) with some functionality removed.
The fork uses the same package name as the original library, so it can be used as a drop-in replacement. It is published to GitHub Packages under the group id `org.opengrabeso/light-surface`.

### Motivation

This fork was created as a workaround for multiple issues in Scala 3 version of Airframe library. Some of the most notable issues are:
- [Cannot get methodsOf for some generic types, esp. in Scala 3](https://github.com/wvlet/airframe/issues/3433)
- [Surface.methodsOf for Enumeration fails in Scala 3](https://github.com/wvlet/airframe/issues/3411)
- [Surface.methodsOf for Seq fails with Scala ](https://github.com/wvlet/airframe/issues/3409)
- [Surface.of sometimes fails for classes using generic types on parameters](https://github.com/wvlet/airframe/issues/3417)

All these issues are related to `Surface.objectFactory` or `ClassMethodSurface.methodCaller`. Once these issues are resolved, this fork is planned to be deprecated.
