addSbtPlugin("com.dwijnand"   % "sbt-dynver"   % "1.3.0")
addSbtPlugin("com.dwijnand"   % "sbt-travisci" % "1.1.0")
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "0.4")

// until https://github.com/softprops/bintray-sbt/pull/69 gets released
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0-8-g6d0c3f8")
resolvers += Resolver.url("2m-sbt-plugin-releases", url("https://dl.bintray.com/2m/sbt-plugin-releases/"))(
  Resolver.ivyStylePatterns)
