# Scalafix rules for scalafix-unused
![CI](https://github.com/tanishiking/scalafix-unused/actions/workflows/ci.yml/badge.svg)

To develop rule:
```
sbt ~tests/test
# edit rules/src/main/scala/fix/Scalafixunused.scala
```


## TODO
- [x] structural type
- [ ] Better diagnositc message for Anonymous given: see AnonymousGiven.scala
- [ ] Do not mark as used the param who referred by named argument. see: Issue2116.scala
- [x] Do not mark as used the param who has overloaded symbol. see: Methods.scala
- [ ] Do not reporte unused for prefix import type in Scala3, see: PrefixScala3.scala
- [x] private object should also be reported in Scala3, see: Objects.scala
- [ ] should be able to parse `package endmarkers:` scalameta parser issue?
- [ ] TODO: test scala3 detect unused field (enum, given, private extension method)
- [ ] export shouldn't be asserted? see: Exports.scala, ExportsPackage.scala
- [ ] support scala3 synthetics
  - See: Issue1749
