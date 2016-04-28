# Home

Welcome to the documentation site for Socrates, the grading automation program.
Here you can find information about how to use the program, how to design
criteria files for the program, and other information.

*   If you're new to Socrates, skip to the next section.

*   If you're a grader who's been asked to grade using Socrates, see the
    [User Guide](/user_guide/).

*   If you're an instructor or a teaching staff member who needs to create
    criteria files for graders to use, see the *For instructors* category
    on the left.

*   If you're a developer who wants to contribute to Socrates, good news!
    Socrates is open source and welcomes contributions.
    See our [GitHub page](https://github.com/abreen/Socrates).


## About Socrates

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
