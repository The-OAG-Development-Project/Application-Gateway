# Create a new Release

To create a new release and make the container image available to the public follow this procedure:

* From _Code_ view select _Releases_ (https://github.com/The-OAG-Development-Project/Application-Gateway/releases)
* Click _Draft a new relase_
* Decide on the new version number (e.g. v0.6.1)
* Provide a title and summary of the changes / fixes (e.g. "Release V.0.6.1", "With this release we fixed a couple of
  vulnerabilities.")
* In the _Choose Tag_ drop-down enter a new tag name matching the version you like to create (e.g. v0.6.1)
* Choose _main_ as the target branch
* Choose checkbox _set as latest release_
* Click _Publish release_ -> This starts the release build. Wait until it is finished.
* Veryfy that the release has worked: https://central.sonatype.com/artifact/org.owasp/oag/overview
* Wait .. eventually also mvnrepository should be updated, can take days. check in https://mvnrepository.com/artifact/org.owasp/oag

Done.