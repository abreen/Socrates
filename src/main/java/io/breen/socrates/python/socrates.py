import sys

EXIT_PASSED = 1
EXIT_FAILED = 2


def pass_test():
    print('passed')
    sys.exit(EXIT_PASSED)


def fail_test():
    print('failed')
    sys.exit(EXIT_FAILED)
