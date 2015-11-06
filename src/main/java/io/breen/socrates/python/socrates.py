"""
This module allows a Python script to provide a custom implementation of a test for Socrates.
Any script can be used as a Socrates test, so long as it imports this module. In all cases, scripts
must end with a call to conclude(). Scripts must not rely on taking input from the standard in.
Scripts may use the standard error; output will be sent to Socrates' standard error stream.

To obtain any parameters to the test specified in the criteria file, scripts may use the
get_parameters() function.

Note: when this module is imported, it waits for JSON-encoded input from Socrates. Therefore,
this module should be imported immediately (i.e., do not import this module inside a function).
"""
import sys

import json

LOGGING = False
EXIT_NORMAL = 0
EXIT_ERROR = 1


def conclude(passes, transcript=None, notes=None):
    """Indicate that the test has concluded with a pass/fail result. If a "transcript" string is
    specified, the string is appended to the Socrates transcript window for the grader to review.
    If a "notes" string is specified, the string is automatically inserted into the notes field
    of the test, and will appear in the grade file underneath the deduction (if any).
    """
    if passes not in [True, False]:
        raise ValueError('result must be True or False')

    for func in _callbacks:
        func()

    resp = json.dumps({
        'error': False,
        'should_pass': passes,
        'transcript': transcript,
        'notes': notes
    })

    if LOGGING:
        print(resp, file=sys.stderr)

    print(resp)

    sys.exit(EXIT_NORMAL)


def error(exc):
    """Indicate that a fatal exception occurred running the test and a test result could not be
    obtained. In this case, Socrates will raise an internal CannotBeAutomatedException, which
    prompts the grader to manually choose a test result.
    """
    for func in _callbacks:
        func()

    resp = json.dumps({
        'error': True,
        'error_type': type(exc).__name__,
        'error_message': str(exc)
    })

    if LOGGING:
        print(resp, file=sys.stderr)

    print(resp)

    sys.exit(EXIT_NORMAL)


def before_exit(func):
    """Register a "callback" function to be run just before Socrates receives a result and the
    script exits. The callback will be run before exiting when either error() or conclude() is
    called. This function may be called many times to add many callbacks.
    """
    _callbacks.append(func)


def get_parameters():
    return _params.copy()


if __name__ == '__main__':
    print('this module cannot be invoked directly; it should be imported by a script')
    sys.exit(EXIT_ERROR)

# wait for Socrates to supply the parameters to the test
_params = json.loads(input())
_callbacks = []
