from xmlrpc.server import SimpleXMLRPCServer
from xmlrpc.server import SimpleXMLRPCRequestHandler

import importlib
import inspect

PORT = 45003

module_name = None
module = None
classes = None
functions = None
variables = None

globalz = {}  # globals() "inside" the imported module
objects = {}


def _wrap(func):
    def inner(*args, **kwargs):
        print('function:', func.__name__)
        print('args:', args)
        print('kwargs:', kwargs)

        try:
            return {'error': False, 'response': func(*args, **kwargs)}
        except BaseException as e:
            return {'error': True, 'errorType': str(type(e).__name__), 'errorMessage': str(e)}

    return inner


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


class RequestHandler(SimpleXMLRPCRequestHandler):
    rpc_paths = ('/xmlrpc',)


server = SimpleXMLRPCServer(('localhost', PORT), requestHandler=RequestHandler)

d = dict(globals(), **locals())
for name, value in d.items():
    if inspect.isfunction(value) and name[0] != '_':
        server.register_function(_wrap(value), name.replace('_', '.'))
        # server.register_function(_wrap(value), name)

server.serve_forever()
