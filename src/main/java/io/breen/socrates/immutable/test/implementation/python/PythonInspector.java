package io.breen.socrates.immutable.test.implementation.python;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.breen.socrates.python.PythonManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PythonInspector {

    private final String moduleName;
    private final ProcessBuilder builder;
    private final Process process;

    public PythonInspector(Path targetModulePath) throws IOException {
        if (!Files.isRegularFile(targetModulePath))
            throw new IllegalArgumentException("module path must be a path to a file");

        String fileName = targetModulePath.getFileName().toString();
        String[] parts = fileName.split("\\.");
        moduleName = parts[0];

        builder = new ProcessBuilder(
                PythonManager.python3Command.toString(), "-B",
                // turns off writing bytecode files (.py[co])
                PythonManager.getPathToSource("tester.py").toString()
        );

        builder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Map<String, String> env = builder.environment();
        env.put(
                "PYTHONPATH",

                /*
                 * Note: this leading path separator is very important! It ensures that PYTHONPATH
                 * has the empty string as an entry, which tells the Python interpreter to look
                 * in the current working directory of the Python process to find modules.
                 */
                System.getProperty("path.separator")
        );

        Path parentDir = targetModulePath.getParent();
        builder.directory(parentDir.toFile());

        process = builder.start();
    }

    private static boolean equals(Object expected, PythonObject other) {
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

            if (!(other.value instanceof List)) return false;

            List otherList = (List)other.value;

            return list.equals(otherList);

        } else if (expected instanceof Object[]) {
            Object[] arr = (Object[])expected;

            if (other == null || !other.type.equals("list")) return false;

            if (!(other.value instanceof List)) return false;

            List otherList = (List)other.value;

            if (arr.length != otherList.size()) return false;

            for (int i = 0; i < arr.length; i++)
                if (!arr[i].equals(otherList.get(i))) return false;

            return true;

        } else if (expected instanceof Map) {
            Map map = (Map)expected;

            if (other == null || !other.type.equals("dict")) return false;

            if (!(other.value instanceof Map)) return false;

            return map.equals(other);
        }

        throw new IllegalArgumentException();
    }

    private Map<String, Object> newRequestMap() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", moduleName);
        return request;
    }

    private boolean isErrorResponse(Map<String, Object> response) {
        return response.containsKey("error") && (boolean)response.get("error");
    }

    private PythonError errorFromResponse(Map<String, Object> response) {
        return new PythonError(
                (String)response.get("error_type"), (String)response.get("error_message")
        );
    }

    private PythonObject toPythonObject(Map<String, Object> response) {
        return new PythonObject(response.get("value"), (String)response.get("type"));
    }

    public boolean variableExists(String variableName) throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = newRequestMap();
        request.put("type", "exists");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "variable");
        targetMap.put("name", variableName);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, Object> response = mapper.readValue(process.getInputStream(), Map.class);

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    public boolean variableEquals(String variableName, Object value) throws IOException, PythonError
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = newRequestMap();
        request.put("type", "eval");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "variable");
        targetMap.put("name", variableName);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, Object> response = mapper.readValue(process.getInputStream(), Map.class);

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return equals(value, toPythonObject(response));
    }

    public boolean canImportModule() throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = newRequestMap();
        request.put("type", "load");

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, Object> response = mapper.readValue(process.getInputStream(), Map.class);

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    public boolean functionExists(String functionName) throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = newRequestMap();
        request.put("type", "exists");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "function");
        targetMap.put("name", functionName);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, Object> response = mapper.readValue(process.getInputStream(), Map.class);

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    public boolean functionProduces(String functionName, List<Object> args,
                                    Map<String, Object> kwargs, String input, Object returnValue,
                                    String output) throws IOException, PythonError
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = newRequestMap();
        request.put("type", "eval");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "function");
        targetMap.put("name", functionName);

        request.put("target", targetMap);

        // parameters of the test, not to the function
        Map<String, Object> parametersMap = new HashMap<>();

        if (args != null && !args.isEmpty()) parametersMap.put("args", args);
        if (kwargs != null && !kwargs.isEmpty()) parametersMap.put("kwargs", kwargs);
        if (input != null) parametersMap.put("input", input);

        request.put("parameters", parametersMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, Object> response = mapper.readValue(process.getInputStream(), Map.class);

        if (isErrorResponse(response)) throw errorFromResponse(response);

        if (output != null) {
            if (!response.containsKey("output")) return false;

            String outputProduced = (String)response.get("output");
            if (!output.equals(outputProduced)) return false;
        }

        return equals(returnValue, toPythonObject(response));
    }

    public class PythonObject {

        public final Object value;
        public final String type;

        public PythonObject(Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }
}
