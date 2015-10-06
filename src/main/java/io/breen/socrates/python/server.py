import sys

import os
import re
import importlib
import inspect
from xmlrpc.server import SimpleXMLRPCServer
from xmlrpc.server import SimpleXMLRPCRequestHandler

LOGGING = True
LOG_FILE = open('server.log', 'a') if LOGGING else None
PATH = '/xmlrpc'
NIL_PATTERN = re.compile(r'<nil ?/>')
NIL_ELEMENT = r'<ex:nil xmlns:ex="http://ws.apache.org/xmlrpc/namespaces/extensions" />'

module_name = None
module = None
classes = None
functions = None
variables = None

globalz = {}  # globals() "inside" the imported module
objects = {}


def log(str):
    if not LOGGING:
        return

    import datetime

    LOG_FILE.write(datetime.datetime.now().isoformat() + ': ' + str + '\n')
    LOG_FILE.flush()

def _wrap(func):
    def inner(*args, **kwargs):
        log('function: ' + func.__name__)
        log('args: ' + str(args))
        log('kwargs: ' + str(kwargs))

        try:
            result = func(*args, **kwargs)
            return {'error': False, 'result': result, 'type': type(result).__name__}
        except SystemExit:
            log('exiting')
            sys.exit(0)
        except BaseException as e:
            log('exception: ' + str(e))
            return {'error': True, 'errorType': type(e).__name__, 'errorMessage': str(e)}

    return inner


def hello():
    return True


def goodbye():
    raise SystemExit


def module_open(name):
    import types

    bad_types = [types.FunctionType, types.LambdaType, types.MethodType, types.ModuleType]

    global module_name, module, classes, functions, variables, globalz
    classes, functions, variables = {}, {}, {}
    globalz = {}

    module_name = name
    module = importlib.import_module(name)
    for member_name, value in inspect.getmembers(module):
        globalz[member_name] = value

        if inspect.isbuiltin(value):
            continue
        elif inspect.isclass(value):
            classes[member_name] = value
        elif inspect.isfunction(value):
            functions[member_name] = value
        elif type(value) not in bad_types:
            variables[member_name] = value

    return True


def eval(code):
    import builtins
    return builtins.eval(code, dict(globals(), **globalz), objects)


def module_hasClass(name):
    return name in classes


def module_hasFunction(name):
    return name in functions


def module_hasVariable(name):
    return name in variables


def variable_eval(name):
    return variables[name]


def function_eval(name, args, kwargs):
    fun = functions[name]
    return fun(*args, **kwargs)


def object_new(class_name, identifier, args, kwargs):
    cls = classes[class_name]
    obj = cls(*args, **kwargs)
    objects[identifier] = obj
    return True


def object_newWithoutInit(class_name, identifier, attrs):
    cls = classes[class_name]
    obj = cls.__new__(cls)

    for key, value in attrs.items():
        setattr(obj, key, value)

    objects[identifier] = obj
    return True


def object_hasAttribute(identifier, attr_name):
    return attr_name in dir(objects[identifier])


def method_eval(object_identifier, method_name, args, kwargs):
    obj = objects[object_identifier]
    method = None

    for name, value in inspect.getmembers(obj, inspect.ismethod):
        if name == method_name:
            method = value
            break

    return method(*args, **kwargs)


class MyRequestHandler(SimpleXMLRPCRequestHandler):
    rpc_paths = (PATH,)


class MyXMLRPCServer(SimpleXMLRPCServer):
    def _marshaled_dispatch(self, data, dispatch_method=None, path=None):
        marshaled = super()._marshaled_dispatch(data, dispatch_method, path)
        marshaled_str = marshaled.decode(self.encoding)
        marshaled_str = re.sub(NIL_PATTERN, NIL_ELEMENT, marshaled_str)
        return marshaled_str.encode(self.encoding)

#
# start of script
#

if len(sys.argv) != 2:
    print('server port must be specified', file=sys.stderr)
    sys.exit(1)

port = int(sys.argv[1])

server = MyXMLRPCServer(('127.0.0.1', port), requestHandler=MyRequestHandler, allow_none=True)

d = dict(globals(), **locals())
for name, value in d.items():
    if inspect.isfunction(value) and name[0] != '_':
        server.register_function(_wrap(value), name.replace('_', '.'))
        # server.register_function(_wrap(value), name)

log('XML-RPC server started in ' + os.getcwd() + ' on port ' + str(port))
server.serve_forever()
