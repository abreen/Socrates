"""
This module allows a Python script to provide a custom implementation of a test for Socrates.
Any script can be used as a Socrates test, so long as it imports this module. In all cases, scripts
must end with a call to one of the test conclusion functions (see source code). Scripts must not
rely on taking input from the standard in.

To obtain any parameters to the test specified in the criteria file, scripts may use the
get_test_parameters() function.

Note: when this module is imported, it waits for JSON-encoded input from Socrates. Therefore,
this module should be imported immediately (i.e., do not import this module inside a function).
"""

import sys

import json

EXIT_PASSED = 10
EXIT_FAILED = 11
EXIT_ERROR = 12

# wait for Socrates to supply the parameters to the test
params = json.loads(input())


def get_parameters():
    return params


def pass_test():
    sys.exit(EXIT_PASSED)


def fail_test():
    sys.exit(EXIT_FAILED)


def raise_error():
    sys.exit(EXIT_ERROR)
