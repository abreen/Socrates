---
layout: topic
title: Installation
authors: [Alexander Breen]
---

Socrates is a Java program, so it can run wherever a JRE can be installed (Mac
OS X, Linux, and Windows). If you'd like to build Socrates from source, see
[Building Socrates](building.html).


## Prerequisites

*   You must have a Java Runtime Environment (JRE) installed (**version 8** or
    newer). If you've downloaded a Java Development Kit (JDK) for writing your
    own Java applications, you already have a JRE.

    You can obtain Oracle's JRE from
    [Oracle's site](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
    It may also be possible to use [OpenJDK](http://openjdk.java.net), but this
    has not been extensively tested.

*   You must have Python 3 installed (version 3.2 or newer). If you don't have
    a Python interpreter installed, or you only have a Python 2 interpreter
    installed, Socrates will show you an error message on startup.

    You can obtain Python 3 from [the Python project's
    site](http://www.python.org).

If you have both of the above, continue to the next section.


## Downloading

Compiled binaries in JAR or OS X `.app` form are available from the [Releases
page on GitHub](https://github.com/abreen/Socrates/releases). Simply click the
appropriate download link for the most recent release and double-click the
downloaded file to start Socrates.


## Uninstalling

To uninstall Socrates, delete the JAR or `.app` package. Apart from files in
your operating system's temporary directory (which you do not need to remove
yourself), Socrates may create a `.socrates.properties` file in your home
directory. This file saves any preferences you may have set using the
application, as well as the location of the Python 3 interpreter on your
system.

*   On OS X or Linux, the `.socrates.properties` file is in your home
    directory. To remove this file, type

        :::text
        rm ~/.socrates.properties

    in your favorite shell.

*   On Windows, the `.socrates.properties` file is in your `Users` folder.
    For example, for the user `Alexander`, the file is located at
    `C:\Users\Alexander\.socrates.properties`. You may need to
    [show hidden
    files](http://windows.microsoft.com/en-us/windows/show-hidden-files) to be
    able to select the file.
