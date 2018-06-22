# Citywasp API [![bintray-badge][]][bintray] [![travis-badge][]][travis]

[bintray]:               https://bintray.com/2m/maven/citywasp-api
[bintray-badge]:         https://api.bintray.com/packages/2m/maven/citywasp-api/images/download.svg
[travis]:                https://travis-ci.org/2m/citywasp-api
[travis-badge]:          https://travis-ci.org/2m/citywasp-api.svg?branch=master

This is a Scala API for [Citybee](https://www.citybee.lt) car sharing application. Currently it is capable of returning the list of all car descriptions and a list of all available cars.

## Running

This library is intended to be used as a dependency from other applications. However this repository includes a small runnable application which lists all available cars. You can run it by one of the following:

### coursier

```bash
coursier launch citywasp:citywasp-api_2.12:1.1 -r bintray:2m/maven -- -J-Dcitywasp.email=<email> -J-Dcitywasp.password=<password>
```

Where `<email>` and `<password>` are the credentials to your Citybee account.

### sbt

Checkout this repository to your computer and then run:

```bash
sbt -Dcitywasp.email=<email> -Dcitywasp.password=<password>
sbt:citywasp-api> run
```

## Usages

The only currently known usage of this library in the wild is the [@kabrioletas](https://twitter.com/kabrioletas) and [@fijatas](https://twitter.com/fijatas) Twitter bots.
