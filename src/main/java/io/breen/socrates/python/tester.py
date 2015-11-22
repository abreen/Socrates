"""
{
'module': 'myprogram',
'target': {'type': 'function', 'name': 'foo'},
'type': 'eval',
'parameters': {'args': [1, 2], 'value': 10}
}

{
'module': 'myprogram',
'target': {'type': 'variable', 'name': 'bar'},
'type': 'exists'
}

{
'module': 'myprogram',
'type': 'load'
}
"""

import sys
import _thread

import threading
import types
import json
import importlib
import inspect
from io import StringIO

LOGGING = False
TIMEOUT = 10  # seconds

globalz = {}  # globals() in the imported module
objects = {}

classes = {}
functions = {}
variables = {}


def log(s):
    print(s, file=sys.stderr)


def conclude(val, output=None, after=None):
    s = json.dumps({
        'error': False,  # no error occurred
        'value': val,  # the value of a variable/value returned by a function or method
        'type': type(val).__name__,  # the type of the value (the type name as a string)
        'output': output,  # any characters sent to the standard out during eval
        'after': after  # if a method is being tested on an instance, its state after a method call
    })

    if LOGGING:
        print(s, file=sys.stderr)

    print(s)

    sys.exit(0)


def error(exc):
    s = json.dumps({
        'error': True,
        'error_type': type(exc).__name__,
        'error_message': str(exc)
    })

    if LOGGING:
        print(s, file=sys.stderr)

    print(s)

    sys.exit(1)


def new(klass, fields):
    """Create and return an instance of the specified class and fill the instance with fields
    as specified by the second argument. This function does *not* call the class' constructor.
    """
    obj = klass.__new__(klass)
    for attr, val in fields.items():
        setattr(obj, attr, val)

    return obj


def find_method(class_name, method_name):
    try:
        for name, value in inspect.getmembers(classes[class_name]):
            if inspect.isfunction(value) and name == method_name:
                return value

    except KeyError as e:
        # the class could not be found
        return None


timer = threading.Timer(TIMEOUT, _thread.interrupt_main)
timer.start()

# wait for a JSON message describing what we should do
msg = json.loads(input())

module = None
try:
    module = importlib.import_module(msg['name'])
except ImportError as e:
    # could not find module
    error(e)

except (SyntaxError, NameError) as e:
    if msg['type'] == 'load':
        conclude(False, type(e).__name__ + ': ' + str(e))
    else:
        error(e)

if msg['type'] == 'load':
    conclude(True)

# catalog the members of this module
for member_name, value in inspect.getmembers(module):
    globalz[member_name] = value

    if inspect.isbuiltin(value):
        continue
    elif inspect.isclass(value):
        classes[member_name] = value
    elif inspect.isfunction(value):
        functions[member_name] = value
    elif type(value) not in [types.FunctionType, types.LambdaType, types.MethodType,
                             types.ModuleType]:
        variables[member_name] = value

if msg['type'] == 'exists':
    target = msg['target']

    if target['type'] == 'variable':
        conclude(target['name'] in variables)
    elif target['type'] == 'function':
        conclude(target['name'] in functions)
    elif target['type'] == 'class':
        conclude(target['name'] in classes)
    elif target['type'] == 'method':
        method_name = target['name']
        class_name = target['class_name']

        method = find_method(class_name, method_name)
        conclude(method is not None)

elif msg['type'] == 'eval':
    target = msg['target']

    if target['type'] == 'variable':
        conclude(variables[target['name']])

    elif target['type'] in ['function', 'method']:
        parameters = msg['parameters']
        args = parameters.get('args', [])
        object_indices = parameters.get('object_indices', [])
        kwargs = parameters.get('kwargs', {})
        in_str = parameters.get('input', '')

        before = parameters.get('before', None)
        after = parameters.get('after', None)

        if target['type'] == 'method':
            before_class = classes[before['class_name']]
            before_fields = before['fields']

            before_obj = new(before_class, before_fields)

        for i in object_indices:
            args[i] = new(classes[args[i]['class_name']], args[i]['fields'])

        in_buf = StringIO(in_str)
        out_buf = StringIO()

        sys.stdin = in_buf
        sys.stdout = out_buf

        try:
            if target['type'] == 'function':
                f = functions[target['name']]
                rv = f(*args, **kwargs)

            elif target['type'] == 'method':
                m = find_method(before['class_name'], target['name'])
                rv = m(before_obj, *args, **kwargs)

        except Exception as e:
            sys.stdin = sys.__stdin__
            sys.stdout = sys.__stdout__
            error(e)

        sys.stdin = sys.__stdin__
        sys.stdout = sys.__stdout__

        output_str = out_buf.getvalue()

        after_fields = None
        if target['type'] == 'method':
            after_fields = {}
            for name, value in inspect.getmembers(before_obj):
                if inspect.isbuiltin(value) or name[0] == '_' or callable(value):
                    continue

                after_fields[name] = value

        conclude(rv, output=output_str, after=after_fields)
