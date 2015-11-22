package io.breen.socrates.test.python;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.breen.socrates.file.python.Object;
import io.breen.socrates.python.PythonManager;
import io.breen.socrates.util.Pair;

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

    private static boolean equals(java.lang.Object expected, ResultObject other) {
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

            return map.equals(other.value);
        }

        throw new IllegalArgumentException();
    }

    private static boolean isPrimitive(java.lang.Object object) {
        return object instanceof String || object instanceof Number || object instanceof Boolean
                || object instanceof Map;
    }

    public static String toPythonString(java.lang.Object o) {
        if (o == null) return "None";

        if (o instanceof Object) {
            /*
             * The case when this object was created using a !python:object definition in a
             * a criteria file. We display this object as if it were a call to the constructor
             * with the keyword arguments of its fields. (This might not be correct Python code,
             * since this constructor may not exist. However, this string is not being eval()'d
             * by any Python code --- it's just for illustration.)
             */
            Object pyObj = (Object)o;
            StringBuilder builder = new StringBuilder();
            builder.append(pyObj.type.typeName);
            builder.append("(");

            int i = 0;
            int numEntries = pyObj.fields.size();
            for (Map.Entry<String, java.lang.Object> entry : pyObj.fields.entrySet()) {
                builder.append(entry.getKey() + "=" + toPythonString(entry.getValue()));
                if (++i != numEntries) builder.append(", ");
            }

            builder.append(")");
            return builder.toString();

        } else if (o instanceof Number) {
            return o.toString();

        } else if (o instanceof String) {
            String s = (String)o;
            return "'" + s + "'";

        } else if (o instanceof Boolean) {
            Boolean b = (Boolean)o;
            if (b) return "True";
            else return "False";

        } else if (o instanceof List) {
            List<java.lang.Object> l = (List)o;

            StringBuilder builder = new StringBuilder();
            builder.append("[");

            int i = 0;
            int numItems = l.size();
            for (java.lang.Object item : l) {
                builder.append(toPythonString(item));
                if (++i != numItems) builder.append(", ");
            }

            builder.append("]");
            return builder.toString();

        } else if (o instanceof Map) {
            Map<java.lang.Object, java.lang.Object> m = (Map)o;

            StringBuilder builder = new StringBuilder();
            builder.append("{");

            int i = 0;
            int numEntries = m.size();
            for (Map.Entry<java.lang.Object, java.lang.Object> entry : m.entrySet()) {
                builder.append(
                        toPythonString(entry.getKey()) + ": " +
                                toPythonString(entry.getValue())
                );

                if (++i != numEntries) builder.append(", ");
            }

            builder.append("}");
            return builder.toString();
        }

        throw new IllegalArgumentException(
                "cannot make Python string for: " + o
        );
    }

    public static String callToString(String functionName, List<java.lang.Object> args) {
        StringBuilder builder = new StringBuilder(functionName);

        builder.append("(");

        for (int i = 0; i < args.size(); i++) {
            builder.append(toPythonString(args.get(i)));

            if (i != args.size() - 1) builder.append(", ");
        }

        builder.append(")");

        return builder.toString();
    }

    private Map<String, java.lang.Object> newRequestMap() {
        Map<String, java.lang.Object> request = new HashMap<>();
        request.put("name", moduleName);
        return request;
    }

    private boolean isErrorResponse(Map<String, java.lang.Object> response) {
        return response.containsKey("error") && (boolean)response.get("error");
    }

    private PythonError errorFromResponse(Map<String, java.lang.Object> response) {
        return new PythonError(
                (String)response.get("error_type"), (String)response.get("error_message")
        );
    }

    private ResultObject toPythonObject(Map<String, java.lang.Object> response) {
        return new ResultObject(response.get("value"), (String)response.get("type"));
    }

    public boolean variableExists(String variableName) throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "exists");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "variable");
        targetMap.put("name", variableName);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    public boolean variableEquals(String variableName, java.lang.Object value)
            throws IOException, PythonError
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "eval");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "variable");
        targetMap.put("name", variableName);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return equals(value, toPythonObject(response));
    }

    /**
     * Asks the Python interpreter to import the Python module. If the Python interpreter is able to
     * import the module, it returns true and this method returns (true, null). If the interpreter
     * cannot import the module due to an error (e.g., a syntax error) at import-time, it returns
     * false and the error string generated by the interpreter.
     *
     * @return A pair indicating what happened while importing the module
     *
     * @throws IOException If a low-level error occurs communicating with the interpreter
     * @throws PythonError If Python exits with an error (in our code, not the student's)
     */
    public Pair<Boolean, String> canImportModule() throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "load");

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) {
            throw errorFromResponse(response);
        }

        boolean passed = (boolean)response.get("value");

        if (passed) {
            return new Pair<>(true, null);
        } else {
            String output = (String)response.get("output");
            return new Pair<>(false, output);
        }
    }

    /**
     * Asks the Python interpreter to check whether a function exists (by name).
     *
     * @return true if the interpreter could find the function, false otherwise
     *
     * @throws IOException If a low-level error occurs communicating with the interpreter
     * @throws PythonError If Python exits with an error
     */
    public boolean functionExists(String functionName) throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "exists");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "function");
        targetMap.put("name", functionName);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    /**
     * Asks the Python interpreter to check whether a class exists (by name).
     *
     * @return true if the interpreter could find the class, false otherwise
     *
     * @throws IOException If a low-level error occurs communicating with the interpreter
     * @throws PythonError If Python exits with an error
     */
    public boolean classExists(String className) throws IOException, PythonError {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "exists");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "class");
        targetMap.put("name", className);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    /**
     * Asks the Python interpreter to check whether a method exists (by name). The name of the class
     * containing the method must also be specified. If the class name specifies a class that
     * doesn't exist, this method throws a PythonError.
     *
     * @return true if the interpreter could find the method, false otherwise
     *
     * @throws IOException If a low-level error occurs communicating with the interpreter
     * @throws PythonError If Python exits with an error
     */
    public boolean methodExists(String className, String functionName)
            throws IOException, PythonError
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "exists");

        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("type", "method");
        targetMap.put("name", functionName);
        targetMap.put("class_name", className);

        request.put("target", targetMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) throw errorFromResponse(response);

        return (boolean)response.get("value");
    }

    /**
     * Asks the Python interpreter to run a Python function with the specified arguments and
     * determine whether it equals the value specified. If the function doesn't produce the expected
     * value and/or output, the Boolean value returned is false. In either case, the string returned
     * is the output produced by the function (if any), followed by the newline character, and the
     * string representation of the return value (i.e., as if the function were evaluated on the
     * Python REPL).
     *
     * @return A pair indicating whether the expected value is the same
     *
     * @throws IOException If a low-level error occurs communicating with the interpreter
     * @throws PythonError If a Python error occurs evaluating the function
     */
    public Pair<Boolean, String> functionProduces(String functionName, List<java.lang.Object> args,
                                                  Map<String, java.lang.Object> kwargs,
                                                  String input, java.lang.Object returnValue,
                                                  String output) throws IOException, PythonError
    {
        return methodProduces(functionName, null, args, kwargs, input, null, returnValue, output);
    }

    /**
     * Asks the Python interpreter to run a Python method with the specified arguments and determine
     * whether it equals the value specified. The "before" state of the called object is also
     * specified. If the method doesn't produce the expected value and/or output, or the fields of
     * the called object that are specified by the "after" object do not match, the Boolean value
     * returned is false. In either case, the string returned is the output produced by the method
     * (if any), followed by the newline character, and the string representation of the return
     * value (i.e., as if the function were evaluated on the Python REPL).
     *
     * @return A pair indicating whether the expected value is the same
     *
     * @throws IOException If a low-level error occurs communicating with the interpreter
     * @throws PythonError If a Python error occurs evaluating the function
     */
    public Pair<Boolean, String> methodProduces(String methodName, Object before,
                                                List<java.lang.Object> args,
                                                Map<String, java.lang.Object> kwargs, String input,
                                                Object after, java.lang.Object returnValue,
                                                String output) throws IOException, PythonError
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, java.lang.Object> request = newRequestMap();
        request.put("type", "eval");

        Map<String, String> targetMap = new HashMap<>();

        if (before != null) targetMap.put("type", "method");
        else targetMap.put("type", "function");

        targetMap.put("name", methodName);

        request.put("target", targetMap);

        // parameters of the test, not to the function
        Map<String, java.lang.Object> parametersMap = new HashMap<>();

        if (args != null && !args.isEmpty()) {
            // keep track of the indices of arguments that should be constructed in Python
            List<Integer> indices = new ArrayList<>(args.size());
            List<java.lang.Object> argsList = new ArrayList<>(args.size());

            for (int i = 0; i < args.size(); i++) {
                java.lang.Object o = args.get(i);

                if (o instanceof Object) {
                    Object obj = (Object)o;

                    Map<String, java.lang.Object> fieldsMap = new HashMap<>();
                    for (Map.Entry<String, java.lang.Object> field : obj.fields.entrySet())
                        // TODO should recursively convert field value
                        fieldsMap.put(field.getKey(), field.getValue());

                    Map<String, java.lang.Object> objMap = new HashMap<>();
                    objMap.put("class_name", obj.type.typeName);
                    objMap.put("fields", fieldsMap);

                    argsList.add(objMap);
                    indices.add(i);
                } else {
                    // argument is of primitive type
                    argsList.add(o);
                }
            }

            parametersMap.put("args", argsList);
            parametersMap.put("object_indices", indices);
        }

        if (kwargs != null && !kwargs.isEmpty()) parametersMap.put("kwargs", kwargs);
        if (input != null) parametersMap.put("input", input);

        if (before != null) {
            Map<String, java.lang.Object> beforeMap = new HashMap<>();
            beforeMap.put("class_name", before.type.typeName);
            beforeMap.put("fields", before.fields);
            parametersMap.put("before", beforeMap);
        }

        request.put("parameters", parametersMap);

        mapper.writeValue(process.getOutputStream(), request);

        Map<String, java.lang.Object> response = mapper.readValue(
                process.getInputStream(),
                Map.class
        );

        if (isErrorResponse(response)) throw errorFromResponse(response);

        String str = "";

        if (response.containsKey("output")) str += response.get("output");

        if (!response.get("type").equals("NoneType")) str += toPythonString(response.get("value"));

        if (output != null)
            if (!response.containsKey("output") || !output.equals(response.get("output")))
                // we expect output, but this function produces no output/incorrect output
                return new Pair<>(false, str);

        if (after != null) {
            Map<String, java.lang.Object> fields = (Map)response.get("after");
            for (Map.Entry<String, java.lang.Object> field : after.fields.entrySet()) {
                java.lang.Object afterValue = fields.get(field.getKey());
                if (!afterValue.equals(field.getValue())) {
                    // TODO should not compare with object equals
                    // object state after method call does not match expected state
                    return new Pair<>(false, str);
                }
            }
        }

        return new Pair<>(equals(returnValue, toPythonObject(response)), str);
    }

    public class ResultObject {

        public final java.lang.Object value;
        public final String type;

        public ResultObject(java.lang.Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }
}
