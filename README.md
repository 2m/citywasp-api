# [Citywasp API][] [![scaladex-badge][]][scaladex] [![ci-badge][]][ci] [![gitter-badge][]][gitter]

[Citywasp API]:       https://github.com/2m/citywasp-api
[scaladex]:           https://index.scala-lang.org/2m/citywasp-api
[scaladex-badge]:     https://index.scala-lang.org/2m/citywasp-api/latest.svg
[ci]:                 https://github.com/2m/citywasp-api/actions
[ci-badge]:           https://github.com/2m/citywasp-api/workflows/ci/badge.svg
[gitter]:             https://gitter.im/2m/general
[gitter-badge]:       https://badges.gitter.im/2m/general.svg

This is a Scala API for [Citybee](https://www.citybee.lt) car sharing application. Currently it is capable of returning the list of all car descriptions and a list of all available cars.

## Running

This library is intended to be used as a dependency from other applications. However this repository includes a small runnable application which lists all available cars. You can run it by one of the following:

### coursier

```bash
coursier launch lt.dvim.citywasp::citywasp-cli:latest.release --
```

### sbt

Checkout this repository to your computer and then run:

```bash
sbt stage
cli/target/universal/stage/bin/citywasp-cli
```

## Usages

The only currently known usage of this library in the wild is the [@kabrioletas](https://twitter.com/kabrioletas) and [@fijatas](https://twitter.com/fijatas) Twitter bots.
