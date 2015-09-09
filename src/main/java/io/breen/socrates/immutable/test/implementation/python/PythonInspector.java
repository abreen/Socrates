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
import java.util.Map;
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
    private static int XMLRPCPort = 45003;
    private static URL XMLRPCURL = null;

    static {
        try {
            XMLRPCURL = new URL("http://127.0.0.1:" + XMLRPCPort + XMLRPCPath);
        } catch (MalformedURLException e) {
            logger.severe("bad URL for XML-RPC server: " + e);
        }
    }

    private final PythonProcessBuilder builder;
    private final Process process;
    private final XmlRpcClient client;

    public PythonInspector(Path targetModulePath) throws IOException {
        if (!Files.isRegularFile(targetModulePath))
            throw new IllegalArgumentException("module path must be a path to a file");

        builder = new PythonProcessBuilder(PythonManager.XMLRPCServer);
        builder.setDirectory(targetModulePath.getParent());
        process = builder.start();

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(XMLRPCURL);

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
                if (hello().equals(Boolean.TRUE)) { // connected
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

    private static Object getResult(Object response) throws PythonError {
        Map<String, Object> map = (Map<String, Object>)response;
        boolean error = (boolean)map.get("error");
        if (error) {
            String type = (String)map.get("errorType");
            String message = (String)map.get("errorMessage");
            throw new PythonError(type, message);
        }

        return map.get("result");
    }

    @Override
    public void close() {
        process.destroy();
    }

    public Object hello() throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.HELLO.methodString, new Object[] {}
        );

        return getResult(response);
    }

    public void openModule(String moduleName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.MODULE_OPEN.methodString, new Object[] {moduleName}
        );

        Object result = getResult(response);
    }

    public Object variableEval(String variableName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.VARIABLE_EVAL.methodString, new Object[] {variableName}
        );

        return getResult(response);
    }

    public boolean moduleHasVariable(String variableName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.MODULE_HASVARIABLE.methodString, new Object[] {variableName}
        );

        return (boolean)getResult(response);
    }
}
