package io.breen.socrates.constructor;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.immutable.file.*;
import io.breen.socrates.immutable.hooks.HookManager;
import io.breen.socrates.immutable.hooks.triggers.*;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.util.*;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;

/**
 * A subclass of the SnakeYAML constructor that knows how to instantiate our custom classes (e.g.,
 * Criteria, File, Test, or TestGroup objects).
 *
 * Integrates closely with io.breen.socrates.immutable.file.FileFactory and
 * io.breen.socrates.immutable.test.TestFactory.
 *
 * @see io.breen.socrates.immutable.file.FileFactory
 * @see io.breen.socrates.immutable.test.TestFactory
 */
public class SocratesConstructor extends SafeConstructor {

    private static final String GROUP_TAG = "!group";
    private static final String FILE_PREFIX = "!file:";
    private static final String TEST_PREFIX = "!test:";

    public Map<String, Resource> staticResources;
    public Map<String, Resource> scriptResources;
    public Map<String, Resource> hookResources;

    public SocratesConstructor() {
        this(null, null, null);
    }

    public SocratesConstructor(Map<String, Resource> staticResources,
                               Map<String, Resource> scriptResources,
                               Map<String, Resource> hookResources)
    {
        this.rootTag = new Tag(Criteria.class);
        this.yamlConstructors.put(this.rootTag, new RootConstruct(this));

        this.yamlConstructors.put(new Tag(GROUP_TAG), new GroupConstruct(this));
        this.yamlMultiConstructors.put(FILE_PREFIX, new FileConstruct(this));
        this.yamlMultiConstructors.put(TEST_PREFIX, new TestConstruct(this));

        this.staticResources = staticResources != null ? staticResources : new HashMap<>();
        this.scriptResources = scriptResources != null ? scriptResources : new HashMap<>();
        this.hookResources = hookResources != null ? hookResources : new HashMap<>();
    }

    private static String getSuffix(String prefix, Node n) {
        // TODO check that tag even exists?
        return n.getTag().getValue().substring(prefix.length());
    }

    public static Double coerceToDouble(Object obj) {
        if (obj instanceof Integer) return ((Integer)obj).doubleValue();

        if (obj instanceof Double) return (Double)obj;

        if (obj == null) return null;

        throw new ClassCastException(obj + " is not coercible to Double");
    }

    /**
     * Given a list of objects which may be TestWithoutFileType objects or TestGroup objects
     * containing TestWithoutFileType members, "traverse" this "tree" of Tests, using the
     * TestFactory to convert all TestWithoutFileType objects to actual instances, using the
     * specified FileType.
     */
    private List<Either<Test, TestGroup>> buildAllTests(List tests, FileType fileType, Node node)
    {
        List<Either<Test, TestGroup>> newList = new LinkedList<>();

        for (Object o : tests) {
            if (o instanceof TestGroup) {
                TestGroup g = (TestGroup)o;

                List<Either<Test, TestGroup>> newMembers = buildAllTests(
                        g.members, fileType, node
                );

                newList.add(new Right<>(new TestGroup(newMembers, g)));

            } else if (o instanceof TestWithoutFileType) {
                TestWithoutFileType t = (TestWithoutFileType)o;

                TestType type = TestType.fromTypeAndString(fileType, t.testType);

                if (type == null) throw new InvalidCriteriaException(
                        node.getStartMark(),
                        "unknown: '" + t.testType + "' test for '" + fileType + "' file"
                );

                try {
                    Test builtTest = TestFactory.buildTest(type, t.map, scriptResources);

                    Map<String, String> hooksMap = (Map)t.map.get("hooks");
                    if (hooksMap != null) {
                        for (Map.Entry<String, String> entry : hooksMap.entrySet()) {
                            String hookType = entry.getKey();
                            String scriptFileName = entry.getValue();

                            TestHook hook = TestHook.fromString(hookType);
                            if (hook == null) throw new InvalidCriteriaException(
                                    node.getStartMark(), "unknown hook: " + hookType
                            );

                            if (!hookResources.containsKey(scriptFileName))
                                throw new MissingResourceException(scriptFileName);

                            HookManager.registerTestHook(
                                    hook, hookResources.get(scriptFileName), builtTest
                            );
                        }
                    }

                    newList.add(new Left<>(builtTest));
                } catch (InvalidTestException e) {
                    throw new InvalidCriteriaException(node.getStartMark(), e.toString());
                }

            } else {
                throw new IllegalArgumentException();
            }
        }

        return newList;
    }

    private class GroupConstruct extends AbstractConstruct {

        private SocratesConstructor cons;

        public GroupConstruct(SocratesConstructor cons) {
            this.cons = cons;
        }

        public Object construct(Node anyNode) {
            MappingNode node;
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
                maxNum = Ceiling.getAny();
            else maxNum = new AtMost<>(maxNumCrit);

            if (maxValueCrit == null)       // was not specified in criteria file
                maxValue = Ceiling.getAny();
            else maxValue = new AtMost<>(maxValueCrit);

            try {
                return new TestGroup(members, maxNum, maxValue);
            } catch (IllegalArgumentException e) {
                throw new InvalidCriteriaException(
                        anyNode.getStartMark(), e.getMessage()
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

            MappingNode node;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid file: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            Map<Object, Object> map = cons.constructMapping(node);

            FileType fileType = FileType.fromString(suffix);

            List tests = (List)map.get("tests");

            if (tests == null) throw new InvalidCriteriaException(
                    anyNode.getStartMark(), "file must have 'tests'"
            );

            File builtFile;
            try {
                builtFile = FileFactory.buildFile(
                        fileType, map, buildAllTests(tests, fileType, anyNode)
                );

                Map<String, String> hooksMap = (Map)map.get("hooks");
                if (hooksMap != null) {
                    for (Map.Entry<String, String> entry : hooksMap.entrySet()) {
                        String hookType = entry.getKey();
                        String scriptFileName = entry.getValue();

                        FileHook hook = FileHook.fromString(hookType);
                        if (hook == null) throw new InvalidCriteriaException(
                                anyNode.getStartMark(), "unknown hook: " + hookType
                        );

                        if (!hookResources.containsKey(scriptFileName))
                            throw new MissingResourceException(scriptFileName);

                        HookManager.registerFileHook(
                                hook, hookResources.get(scriptFileName), builtFile
                        );
                    }
                }

            } catch (InvalidFileException e) {
                throw new InvalidCriteriaException(anyNode.getStartMark(), e.toString());
            }

            return builtFile;
        }
    }

    private class TestConstruct extends PrefixConstruct {

        public TestConstruct(SocratesConstructor cons) {
            super(cons);
        }

        public Object construct(Node anyNode) {
            String suffix = getSuffix(TEST_PREFIX, anyNode);

            MappingNode node;
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
            MappingNode node;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid top-level: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            /*
             * Continue to construct this mapping. This should use the other constructors
             * in this file (e.g., the FileConstructor) recursively before returning
             * here.
             */
            Map<Object, Object> map = cons.constructMapping(node);

            /*
             * Get the assignment name.
             */
            String name;
            try {
                name = (String)map.get("assignment_name");
                if (name == null) throw new NullPointerException();
                map.remove("assignment_name");
            } catch (ClassCastException e) {
                String msg = "invalid type for 'assignment_name': should be string";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            } catch (NullPointerException e) {
                throw new InvalidCriteriaException(
                        anyNode.getStartMark(), "missing 'assignment_name'"
                );
            }

            /*
             * Get any hooks, if they are specified. If any hooks are specified, we
             * make sure that the appropriate hook resources were also loaded. If not,
             * we are missing the resources needed to run one or more required tasks.
             * In this case, we throw an exception. Otherwise, we register the hook.
             * An InvalidCriteriaException can also be thrown if the hook type is invalid.
             */
            Map<String, String> hooksMap = (Map)map.get("hooks");
            if (hooksMap != null) {
                for (Map.Entry<String, String> entry : hooksMap.entrySet()) {
                    String hookType = entry.getKey();
                    String scriptFileName = entry.getValue();

                    Hook hook = Hook.fromString(hookType);
                    if (hook == null) throw new InvalidCriteriaException(
                            anyNode.getStartMark(), "unknown hook: " + hookType
                    );

                    if (!hookResources.containsKey(scriptFileName))
                        throw new MissingResourceException(scriptFileName);

                    HookManager.register(hook, hookResources.get(scriptFileName));
                }

                map.remove("hooks");
            }

            /*
             * Get files.
             */
            List<File> files = (List)map.get("files");
            map.remove("files");

            /*
             * If there are still key-value pairs in the map, there is data we did not
             * expect. In this case, we can throw an InvalidCriteriaException.
             */
            if (map.size() != 0) {
                StringJoiner joiner = new StringJoiner(", ");
                map.forEach((k, v) -> joiner.add(k.toString()));
                throw new InvalidCriteriaException(
                        "unexpected top-level key(s): " + joiner.toString()
                );
            }

            return new Criteria(
                    name, files, staticResources, scriptResources, hookResources
            );
        }
    }

    /**
     * Represents a test parsed from the YAML file which has an type (i.e., it has a "!test:..."
     * tag) but cannot yet be associated with a file type (i.e., we do not yet know the type of the
     * file that lists this test). We need to create temporary instances of this class so that the
     * YAML parser will parse the values in the test, and then we will use TestFactory when control
     * is passed back to the file constructor.
     */
    private class TestWithoutFileType {

        public final String testType;
        public final Map<Object, Object> map;

        public TestWithoutFileType(String testType, Map<Object, Object> map) {
            this.testType = testType;
            this.map = map;
        }
    }
}
