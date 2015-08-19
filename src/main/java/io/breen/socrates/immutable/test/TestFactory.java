package io.breen.socrates.immutable.test;

import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.immutable.criteria.Resource;

import java.util.Map;

/**
 * This factory's single static method takes a TestType and a map of key-value pairs from
 * a criteria file, and uses the data specified in the map to create the appropriate
 * instance of the specified TestType.
 */
public class TestFactory {

    /**
     * @throws InvalidTestException If the map passed in does not contain valid data
     * enough to create an instance of the desired TestType
     */
    public static Test buildTest(TestType type, Map map, Map<String, Resource> scripts)
            throws InvalidTestException
    {
        switch (type) {
        case SCRIPT: {
            double deduction = getDeduction(type, map);
            String description = getString(type, map, "description");
            String scriptName = getString(type, map, "script");

            Resource script = scripts.get(scriptName);
            if (script == null)
                throw new InvalidTestException(type, "cannot find required script: " + scriptName);

            return new io.breen.socrates.immutable.test.implementation.any.ScriptTest(deduction, description, script);
        } case ALWAYS_PASSING_PLAIN: {
            return new io.breen.socrates.immutable.test.implementation.plain.AlwaysPassingTest(0);
        }
        case REVIEW_PLAIN:
        case REVIEW_PYTHON: {
            double deduction = getDeduction(type, map);
            String description = getString(type, map, "description");

            if (type == TestType.REVIEW_PLAIN)
                return new io.breen.socrates.immutable.test.implementation.plain.ReviewTest(deduction, description);
            else if (type == TestType.REVIEW_PYTHON)
                return new io.breen.socrates.immutable.test.implementation.python.ReviewTest(deduction, description);
        }
        default:
            throw new IllegalArgumentException("invalid test type to build");
        }
    }

    private static double getDeduction(TestType type, Map map)
            throws InvalidTestException
    {
        Double deduction;
        try {
            deduction = SocratesConstructor.coerceToDouble(map.get("deduction"));
            if (deduction == null) throw new NullPointerException();
        } catch (ClassCastException e) {
            throw new InvalidTestException(type, "'deduction' field: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new InvalidTestException(type, "missing 'deduction' field");
        }

        return deduction;
    }

    private static String getString(TestType type, Map map, String key)
            throws InvalidTestException
    {
        String value;
        try {
            value = (String)map.get(key);
            if (value == null) throw new NullPointerException();
        } catch (ClassCastException e) {
            throw new InvalidTestException(
                    type,
                    "'" + key + "' field: " + e.getMessage()
            );
        } catch (NullPointerException e) {
            throw new InvalidTestException(type, "missing '" + key + "' field");
        }

        return value;
    }
}
