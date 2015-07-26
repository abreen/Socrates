package io.breen.socrates;

import java.util.ArrayList;
import java.util.List;

public class SingletonTestGroup extends TestGroup {

    private final Test test;

    public SingletonTestGroup(Test test) {
        this.test = test;
    }

    public List<Deduction> runTests() {
        List<Deduction> list = new ArrayList<>(1);
        Deduction deduction = test.run();

        if (deduction != null)
            list.add(deduction);

        return list;
    }

    public List<Deduction> getPossibleDeductions() {
        List<Deduction> list = new ArrayList<>(1);
        list.add(test.getDeduction());
        return list;
    }
}
