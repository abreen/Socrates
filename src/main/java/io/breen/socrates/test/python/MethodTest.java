package io.breen.socrates.test.python;


public abstract class MethodTest extends FunctionTest {

    /**
     * This empty constructor is used by SnakeYAML for the extenders of this class that are
     * instantiated from a criteria file.
     */
    public MethodTest() {}

    public MethodTest(double deduction, String description) {
        super(deduction, description);
    }
}
