package io.breen.socrates.constructor;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.file.FileFactory;
import io.breen.socrates.immutable.file.FileType;
import io.breen.socrates.immutable.file.InvalidFileException;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.util.Either;
import io.breen.socrates.util.Left;
import io.breen.socrates.util.Right;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A subclass of the SnakeYAML constructor that knows how to instantiate
 * our custom classes (e.g., Criteria, File, Test, or TestGroup objects).
 */
public class SocratesConstructor extends SafeConstructor {
    private static final String GROUP_TAG = "!group";
    private static final String FILE_PREFIX = "!file:";
    private static final String TEST_PREFIX = "!test:";

    private class GroupConstruct extends AbstractConstruct {
        private SocratesConstructor cons;

        public GroupConstruct(SocratesConstructor cons) {
            this.cons = cons;
        }

        public Object construct(Node anyNode) {
            MappingNode node = null;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid file: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            Map<Object, Object> map = cons.constructMapping(node);

            List members = (List)map.get("members");

            if (members == null) throw new InvalidCriteriaException(
                    anyNode.getStartMark(), "test group cannot be empty"
            );

            Ceiling<Integer> maxNum;
            Ceiling<Double> maxValue;

            Integer maxNumCrit = (Integer)map.get("max");

            Double maxValueCrit = coerceToDouble(map.get("max_value"));

            if (maxNumCrit == null)         // was not specified in criteria file
                maxNum = Ceiling.ANY;
            else
                maxNum = new AtMost<Integer>(maxNumCrit);

            if (maxValueCrit == null)       // was not specified in criteria file
                maxValue = Ceiling.ANY;
            else
                maxValue = new AtMost<Double>(maxValueCrit);

            try {
                return new TestGroup(members, maxNum, maxValue);
            } catch (IllegalArgumentException e) {
                throw new InvalidCriteriaException(
                        anyNode.getStartMark(),
                        e.getMessage()
                );
            }
        }
    }

    private abstract class PrefixConstruct extends AbstractConstruct {
        protected SocratesConstructor cons;

        public PrefixConstruct(SocratesConstructor cons) {
            this.cons = cons;
        }

        public abstract Object construct(Node node);
    }

    private class FileConstruct extends PrefixConstruct {
        public FileConstruct(SocratesConstructor cons) {
            super(cons);
        }

        public Object construct(Node anyNode) {
            String suffix = getSuffix(FILE_PREFIX, anyNode);

            MappingNode node = null;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid file: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            Map<Object, Object> map = cons.constructMapping(node);

            FileType fileType = FileType.fromID(suffix);

            List tests = (List)map.get("tests");

            if (tests == null) throw new InvalidCriteriaException(
                    anyNode.getStartMark(), "file must have 'tests'"
            );

            try {
                return FileFactory.buildFile(
                        fileType, map, buildAllTests(tests, fileType, anyNode)
                );
            } catch (InvalidFileException e) {
                throw new InvalidCriteriaException(anyNode.getStartMark(), e.toString());
            }
        }
    }

    private class TestConstruct extends PrefixConstruct {
        public TestConstruct(SocratesConstructor cons) {
            super(cons);
        }

        public Object construct(Node anyNode) {
            String suffix = getSuffix(TEST_PREFIX, anyNode);

            MappingNode node = null;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid test: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            Map<Object, Object> map = cons.constructMapping(node);
            return new TestWithoutFileType(suffix, map);
        }

    }

    /**
     * Class that constructs the root object of the YAML file --- the Criteria object.
     */
    private class RootConstruct extends AbstractConstruct {
        private SocratesConstructor cons;

        public RootConstruct(SocratesConstructor cons) {
            this.cons = cons;
        }

        public Object construct(Node anyNode) {
            MappingNode node = null;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid top-level: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            Map<Object, Object> map = cons.constructMapping(node);

            String name;
            try {
                name = (String)map.get("assignment_name");
                if (name == null) throw new NullPointerException();
            } catch (ClassCastException e) {
                String msg = "invalid type for 'assignment_name': should be string";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            } catch (NullPointerException e) {
                throw new InvalidCriteriaException(
                        anyNode.getStartMark(), "missing 'assignment_name'"
                );
            }

            List<File> files = (List)map.get("files");

            return new Criteria(name, files);
        }
    }

    public SocratesConstructor() {
        this.rootTag = new Tag(Criteria.class);
        this.yamlConstructors.put(this.rootTag, new RootConstruct(this));

        this.yamlConstructors.put(new Tag(GROUP_TAG), new GroupConstruct(this));
        this.yamlMultiConstructors.put(FILE_PREFIX, new FileConstruct(this));
        this.yamlMultiConstructors.put(TEST_PREFIX, new TestConstruct(this));
    }

    private static String getSuffix(String prefix, Node n) {
        // TODO check that tag even exists?
        return n.getTag().getValue().substring(prefix.length());
    }

    public static Double coerceToDouble(Object obj) {
        if (obj instanceof Integer)
            return ((Integer)obj).doubleValue();

        if (obj instanceof Double)
            return (Double)obj;

        if (obj == null)
            return null;

        throw new ClassCastException(obj + " is not coercible to Double");
    }

    /**
     * Represents a test parsed from the YAML file which has an type (i.e., it has
     * a "!test:..." tag) but cannot yet be associated with a file type (i.e., we
     * do not yet know the type of the file that lists this test). We need to create
     * temporary instances of this class so that the YAML parser will parse the
     * values in the test, and then we will use TestFactory when control is passed
     * back to the file constructor.
     */
    private class TestWithoutFileType {
        public final String testType;
        public final Map<Object, Object> map;

        public TestWithoutFileType(String testType, Map<Object, Object> map) {
            this.testType = testType;
            this.map = map;
        }
    }

    /**
     * Given a list of objects which may be TestWithoutFileType objects or
     * TestGroup objects containing TestWithoutFileType members, "traverse" this
     * "tree" of Tests, using the TestFactory to convert all TestWithoutFileType
     * objects to actual instances, using the specified FileType.
     */
    private List<Either<Test, TestGroup>> buildAllTests(List tests, FileType fileType, Node node) {
        List<Either<Test, TestGroup>> newList = new LinkedList<>();

        for (Object o : tests) {
            if (o instanceof TestGroup) {
                TestGroup g = (TestGroup)o;

                List<Either<Test, TestGroup>> newMembers = buildAllTests(g.getMembers(), fileType, node);

                newList.add(new Right<>(new TestGroup(newMembers, g)));

            } else if (o instanceof TestWithoutFileType) {
                TestWithoutFileType t = (TestWithoutFileType)o;

                TestType type = TestType.fromTypeAndID(fileType, t.testType);

                if (type == null) throw new InvalidCriteriaException(
                        node.getStartMark(),
                        "unknown: '" + t.testType + "' test for '" + fileType + "' file"
                );

                try {
                    newList.add(new Left<>(TestFactory.buildTest(type, t.map)));
                } catch (InvalidTestException e) {
                    throw new InvalidCriteriaException(node.getStartMark(), e.toString());
                }

            } else {
                throw new IllegalArgumentException();
            }
        }

        return newList;
    }
}
