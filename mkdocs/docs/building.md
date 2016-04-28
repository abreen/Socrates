---
layout: topic
title: Building Socrates
authors: [Alexander Breen]
---

Socrates can be built with Maven. Its exact dependencies are listed in
the `pom.xml` file. Most of the dependencies will be automatically retrieved
from Maven Central when you first run `mvn` from within this repository,
with the exception of one:

1.  [`pyfinder`](http://github.com/abreen/pyfinder), a Java library that can
    be built with Maven. Use `git clone` to obtain the code, Maven to build a
    JAR, then "install" the JAR into your local Maven repository:

        :::text
        git clone https://github.com/abreen/pyfinder.git
        cd pyfinder
        mvn package
        mvn install:install-file -Dfile=target/pyfinder-0.1.1.jar -DpomFile=pom.xml

Note that you may need to change the version number in the above command to
match the most recent release.

Once these dependencies are installed, run `mvn test` from this repository
to run Socrates' unit tests.
