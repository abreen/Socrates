package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.python.PythonProcessBuilder;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
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
    private static Path XMLRPCServerCode = null;

    static {
        URL serverURL = PythonInspector.class.getResource("server.py");
        if (serverURL != null) {
            logger.severe("cannot find XML-RPC server");

            try {
                URI uri = serverURL.toURI();
                XMLRPCServerCode = Paths.get(uri);
            } catch (Exception e) {
                logger.severe("error setting up path to XML-RPC server:" + e);
            }
        }

        try {
            XMLRPCURL = new URL("http://127.0.0.1:" + XMLRPCPort + XMLRPCPath);
        } catch (MalformedURLException e) {
            logger.severe("bad URL for XML-RPC server:" + e);
        }
    }

    private final PythonProcessBuilder builder;
    private final Process process;
    private final XmlRpcClient client;

    public PythonInspector(Path targetModulePath) throws IOException {
        if (!Files.isRegularFile(targetModulePath))
            throw new IllegalArgumentException("module path must be a path to a file");

        builder = new PythonProcessBuilder(XMLRPCServerCode);
        builder.setDirectory(targetModulePath.getParent());
        process = builder.start();

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(XMLRPCURL);

        client = new XmlRpcClient();
        client.setConfig(config);
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
    public void close() throws Exception {
        process.destroy();
    }

    public void openModule(String moduleName) throws XmlRpcException, PythonError {
        Object response = client.execute(
                RPCMethod.MODULE_OPEN.methodString, new Object[] {moduleName}
        );

        Object result = getResult(response);
    }
}
