1.  **An automated test turned yellow. What does that mean?**

    It means Socrates attempted to run the test, but an exception occurred.
    Usually, this means that the code being tested caused a runtime exception.
    This (rarely) can occur if there's a bug in Socrates' own code that's
    running the test.

    Socrates will always print details about the exception to the standard
    error when this occurs. It will also send this output to the Developer
    Console, which you can open from the *Window* menu. This is different from
    the *Transcript* pane, which will *not* show details about the exception in
    most cases.

2.  **Socrates failed an automated test, but I don't know why.
    How can I find out?**

    The *Transcript* pane contains an overview of the steps Socrates took to
    run a particular test. Usually this pane contains the exact return value
    and/or output of the code being tested.

    Depending on how the test is implemented, Socrates may also fill the [notes
    field](ui.html#notes) with information about why the test failed, so that
    students can see the incorrect result their code produced in the grade
    report.
