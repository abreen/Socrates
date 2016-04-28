Depending on how you [acquired Socrates](/installation/), you should have
downloaded either a JAR file or a `.app` package.

To start Socrates, simply double-click on the file you downloaded.

!!! note
    If you see an error dialog about needing a Java runtime or a Python
    interpreter, see the [installation instructions](/installation/) and
    be sure you have the necessary prerequisites.

# Starting via the command line

Socrates may also be started via the command line. For all platforms, Socrates
accepts the same command line arguments:

*   The `--criteria` argument, after which you must include a single `=`, then
    the path to a criteria file (or criteria package). For example:

        :::text
        --criteria=/home/ajb/ps0/criteria.yml

*   The `--submissions` argument, after which you must include a single `=`,
    then a comma-separated list of *directories* (folders) containing student
    submissions. For example, if I have submission directories for the students
    `ajsmith`, `kjones` and `lixen` in `/home/ajb/ps0`, I could specify:

        :::text
        --submissions=/home/ajb/ps0/ajsmith,/home/ajb/ps0/kjones,/home/ajb/ps0/lixen

    This would cause those submissions to be added to the window Socrates opens
    when it loads a criteria file. **If `--criteria` is not specified, or there
    is an error opening the criteria file, this argument does nothing.**

Before specifying the above command line arguments, you'll need to direct your
command line environment to the JAR file or `.app` package. This varies depending
on your operating system:

*   **Mac OS X**. Open the Terminal application by using Spotlight to search
    for it, finding it in the Utilities folder in your Applications folder, or
    in the Other folder from Launchpad.

    Once Terminal is open, type

        :::text
        java -jar /path/to/Socrates.jar

    followed by any Socrates command line arguments. Replace
    `/path/to/Socrates.jar` with the path to the JAR you downloaded. Or,
    instead, click and drag the icon from a Finder window onto the Terminal
    window after typing `java -jar` and OS X will insert the absolute path for
    you.

*   **Windows**. Open the Command Prompt program by finding it in the Start
    menu, or by typing the key combination Windows + R to open the Run prompt,
    and typing `cmd.exe` in the prompt.

    Once the Command Prompt program is open, type

        :::text
        java.exe -jar path\to\Socrates.jar

    followed by any Socrates command line arguments. Replace
    `path\to\Socrates.jar` with the path to the JAR you downloaded.

