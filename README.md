# Socrates

Socrates is a Java program that was written to make grading easier. It provides
a single-window environment that allows graders to navigate the files that
constitute a student submission. It also supports automating repetitive or
programmable tasks that are ordinarily done by hand (for example, evaluating
functions and checking their return value, output, etc.).

To this end, the program provides tools to run pre-programmed tests defined by
a grading coordinator in a criteria. A criteria is a file that describes the
required parts of a submission (the names of files that should be graded), and
any number of tests designed by the coordinator, which may be automatable or
manual. Reading this file, Socrates determines which parts of a submission are
present, and runs automated tests for the parts of a submission that are
present. Missing parts of a submission or failed tests constitute the
deductions for a particular submission.

Obviously, there are some cases in which a test for Socrates cannot be feasibly
designed. For example, examining a solution for proper code style or comments
must be done by a human grader. In this case, a "test" is written that requires
the human grader to make the pass/fail decision. These manual tests will have
outcomes until the human grader makes the pass/fail decision.

The current version of Socrates is a port of the
[original Python version](http://github.com/abreen/socrates.py) to Java, and
reads criteria files that are incompatible with the older version.


## Building Socrates

Socrates can be built with Maven. Its exact dependencies are listed in
the `pom.xml` file. Most of the dependencies will be automatically retrieved
from Maven Central when you first run `mvn` from within this repository,
with the exception of two:

1.  [`pyfinder`](http://github.com/abreen/pyfinder), a Java library that can
    be built with Maven. Use `git clone` to obtain the code, Maven to build a
    JAR, then "install" the JAR into your local Maven repository:

        git clone https://github.com/abreen/pyfinder.git
        cd pyfinder
        mvn package
        mvn install:install-file -Dfile=target/pyfinder-0.1.1.jar -DpomFile=pom.xml

2.  [Jygments](https://github.com/tliron/jygments), a Java library that
    can be built with [Apache Ant](http://ant.apache.org). Its source code is
    available on GitHub. Use Ant to build a JAR, then ask Maven to "install"
    the JAR into your local Maven repository:

        git clone https://github.com/tliron/jygments.git
        cd jygments/build
        ant
        mvn install:install-file -Dfile=distribution/content/libraries/jars/com.threecrickets.jygments/jygments/0.9.3/jygments.jar -DgroupId=com.threecrickets.jygments -DartifactId=jygments -Dversion=0.9.3 -Dpackaging=jar

Note that with both of these dependencies, you may need to change the version
numbers in the above commands to match the most recent releases.

Once these dependencies are installed, run `mvn test` from this repository
to run Socrates' unit tests.
