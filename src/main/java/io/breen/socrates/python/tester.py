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

import types
import json
import importlib
import inspect

globalz = {}  # globals() in the imported module
objects = {}

classes = {}
functions = {}
variables = {}


def log(s):
    print(s, file=sys.stderr)


def conclude(val, output=None):
    print(json.dumps({
        'error': False,  # no error occurred
        'value': val,  # the value of a variable/value returned by a function
        'type': type(val).__name__,  # the type of the value (the type name as a string)
        'output': output  # any characters sent to the standard out during eval
    }))
    sys.exit(0)


def error(exc):
    print(json.dumps({
        'error': True,
        'error_type': type(exc).__name__,
        'error_message': str(exc)
    }))
    sys.exit(1)


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
    elif target['type'] == 'classes':
        conclude(target['name'] in classes)

elif msg['type'] == 'eval':
    target = msg['target']

    if target['type'] == 'variable':
        conclude(variables[target['name']])

    elif target['type'] == 'function':
        parameters = msg['parameters']
        args = parameters.get('args', [])
        kwargs = parameters.get('kwargs', {})
        in_str = parameters.get('input', '')

        rv, output_str = None, None
        try:
            # TODO provide standard in, if necessary
            rv = functions[target['name']](*args, **kwargs)
            output_str = ''
        except Exception as e:
            error(e)

        # TODO capture standard out
        conclude(rv, output_str)
