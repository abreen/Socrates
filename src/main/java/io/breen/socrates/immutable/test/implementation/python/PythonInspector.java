package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.Globals;
import io.breen.socrates.python.PythonManager;
import io.breen.socrates.python.PythonProcessBuilder;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * A class providing the ability to acquire information about a Python module (such as its class,
 * function and variable definitions) by communicating to an XML-RPC server that can load the module
 * into a running Python interpreter. The XML-RPC server also supports evaluating functions,
 * creating objects of user-defined types, and calling methods on those objects.
 *
 * An instance of this class uses a PythonProcessBuilder to start the XML-RPC server, and then uses
 * the Apache XML-RPC client library to communicate with the interpreter.
 */
public class PythonInspector implements AutoCloseable {

    private enum RPCMethod {
        HELLO("hello"),
        GOODBYE("goodbye"),
        MODULE_OPEN("module.open"),
        MODULE_HASCLASS("module.hasClass"),
        MODULE_HASFUNCTION("module.hasFunction"),
        MODULE_HASVARIABLE("module.hasVariable"),
        VARIABLE_EVAL("variable.eval"),
        FUNCTION_EVAL("function.eval"),
        OBJECT_NEW("object.new"),
        OBJECT_NEWWITHOUTINIT("object.newWithoutInit"),
        OBJECT_HASATTRIBUTE("object.hasAttribute"),
        METHOD_EVAL("method.eval");

        public final String methodString;

        RPCMethod(String methodString) {
            this.methodString = methodString;
        }
    }

    private static Logger logger = Logger.getLogger(PythonInspector.class.getName());
    private static String XMLRPCPath = "/xmlrpc";

    private final PythonProcessBuilder builder;
    private final Process process;
    private final XmlRpcClient client;

    public PythonInspector(Path targetModulePath) throws IOException {
        if (!Files.isRegularFile(targetModulePath))
            throw new IllegalArgumentException("module path must be a path to a file");

        int port = 45000;

        URL url;
        try {
            url = new URL("http://127.0.0.1:" + port + XMLRPCPath);
        } catch (MalformedURLException e) {
            logger.severe("could not form URL for XML-RPC server: " + e);
            throw e;
        }

        builder = new PythonProcessBuilder(PythonManager.XMLRPCServer, Integer.toString(port));
        builder.setDirectory(targetModulePath.getParent());
        process = builder.start();

        try {
            int exitValue = process.exitValue();
            logger.severe("XMLRPC process exited with code " + exitValue);
            throw new IOException("XMLRPC process exited");

        } catch (IllegalThreadStateException ignored) {
            // a good sign: the process is running
        }

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(url);
        config.setEnabledForExtensions(true);

        client = new XmlRpcClient();
        client.setConfig(config);

        int ms = 0;
        for (int i = 1; i <= 1024; i *= 2) {
            try {
                Thread.sleep(i);
                ms += i;
            } catch (InterruptedException x) {
                return;
            }

            try {
                int status = process.exitValue();
                if (status != Globals.NORMAL_EXIT_CODE) {
                    String msg = "Python process exited with code " + status;
                    throw new IOException(msg);
                }

            } catch (IllegalThreadStateException ignored) {}

            try {
                if (hello()) { // connected
                    break;
                } else {
                    logger.severe("did not get true from hello() call");
                    throw new IOException();
                }
            } catch (XmlRpcException ignored) {     // server may not be up yet
            } catch (PythonError x) {
                logger.severe("got PythonError with hello() call");
                throw new IOException();
            }
        }

        logger.info("waited " + ms + " ms for XML-RPC server");
    }

    private static PythonObject getPythonObject(Object response) throws PythonError {
        Map<String, Object> map = (Map<String, Object>)response;
        boolean error = (boolean)map.get("error");
        if (error) {
            String type = (String)map.get("errorType");
            String message = (String)map.get("errorMessage");
            throw new PythonError(type, message);
        }

        Object value = map.get("result");
        String type = (String)map.get("type");
        return new PythonObject(value, type);
    }

    public static boolean equals(Object expected, PythonObject other) {
        if (expected == null) {
            return other.value == null;

        } else if (expected instanceof Boolean) {
            Boolean b = (Boolean)expected;

            if (other == null || !other.type.equals("bool")) return false;

            return b.equals(other.value);

        } else if (expected instanceof Number) {
            Double expectedDouble = ((Number)expected).doubleValue();

            if (other == null || (!other.type.equals("int") && !other.type.equals("float")))
                return false;

            Number otherNumber = (Number)other.value;
            Double otherDouble = otherNumber.doubleValue();

            return expectedDouble.equals(otherDouble);

        } else if (expected instanceof String) {
            String s = (String)expected;

            if (other == null || !other.type.equals("str")) return false;

            return s.equals(other.value);

        } else if (expected instanceof List) {
            List list = (List)expected;

            if (other == null || !other.type.equals("list")) return false;

            if (!(other.value instanceof Object[])) return false;

            Object[] objArr = (Object[])other.value;

            if (list.size() != objArr.length) return false;

            for (int i = 0; i < objArr.length; i++) {
                if (!list.get(i).equals(objArr[i])) return false;
            }

            return true;

        } else if (expected instanceof Map) {
            Map map = (Map)expected;

            if (other == null || !other.type.equals("dict")) return false;

            if (!(other.value instanceof Map)) return false;

            return map.equals(other);
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void close() {
        try {
            goodbye();
        } catch (XmlRpcException x) {
            logger.severe("XMLRPC error sending goodbye message");
        }

        process.destroy();
    }

    public boolean hello() throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.HELLO.methodString, new Object[] {}
        );

        return (boolean)getPythonObject(response).value;
    }

    public void goodbye() throws XmlRpcException {
        client.execute(
                RPCMethod.GOODBYE.methodString, new Object[] {}
        );
    }

    public void openModule(String moduleName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.MODULE_OPEN.methodString, new Object[] {moduleName}
        );

        getPythonObject(response);
    }

    public PythonObject variableEval(String variableName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.VARIABLE_EVAL.methodString, new Object[] {variableName}
        );

        return getPythonObject(response);
    }

    public boolean moduleHasVariable(String variableName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.MODULE_HASVARIABLE.methodString, new Object[] {variableName}
        );

        return (boolean)getPythonObject(response).value;
    }

    public boolean moduleHasFunction(String functionName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.MODULE_HASFUNCTION.methodString, new Object[] {functionName}
        );

        return (boolean)getPythonObject(response).value;
    }

    public PythonObject functionEval(String functionName, List<Object> args)
            throws XmlRpcException, PythonError
    {
        Object response = client.execute(
                RPCMethod.FUNCTION_EVAL.methodString,
                new Object[] {functionName, args, new HashMap<>()}
        );

        return getPythonObject(response);
    }

    public static class PythonObject {

        public final Object value;
        public final String type;

        public PythonObject(Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }
}
