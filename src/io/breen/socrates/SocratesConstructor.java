package io.breen.socrates;

import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.*;

import java.util.List;
import java.util.Map;

/**
 * A subclass of the SnakeYAML constructor that knows how to instantiate
 * our custom classes (e.g., File, Test, or TestGroup objects).
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

        public Object construct(Node node) {
            Map<Object, Object> map = cons.constructMapping((MappingNode)node);

            List<Test> members = (List<Test>)map.get("members");

            int max = (Integer)map.getOrDefault("max", members.size());
            int maxValue = (Integer)map.get("max_value");

            if (max < 0 || maxValue < 0) {
                Mark mark = node.getStartMark();
                throw new InvalidCriteriaException(mark, "'max' or 'max_value' cannot "
                + "be negative");
            }

            if (max == 0) {
                Mark mark = node.getStartMark();
                throw new InvalidCriteriaException(mark, "maximum cannot be zero");
            }

            if (max > members.size()) {
                Mark mark = node.getStartMark();
                throw new InvalidCriteriaException(mark, "maximum cannot be greater than "
                    + "the number of tests in the group");
            }

            if (max == 1 && members.size() == 1) {
                return new SingletonTestGroup(members.get(0));
            }

            return null;
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

        public Object construct(Node node) {
            String suffix = getSuffix(FILE_PREFIX, node);

            //System.out.println("constructing a " + suffix + " file");

            Map<Object, Object> map = cons.constructMapping((MappingNode)node);
            //String path = (String)map.get("path");
            //Double pointValue = (Double)map.get("point_value");

            // loop through test listed, using the cons to construct them
            List<Object> tests = (List<Object>)map.get("tests");

            /*
            if (tests != null) {
                for (Object el : tests) {
                    System.out.println(el);
                }
            }
            */

            File f = null;
            switch (suffix) {
            case "python":
                //f = new PlainFile(path, pointValue);
                break;
            case "plain":
                //f = new PlainFile(path, pointValue);
                break;
            }

            return f;
        }
    }

    private class TestConstruct extends PrefixConstruct {
        public TestConstruct(SocratesConstructor cons) {
            super(cons);
        }

        public Object construct(Node node) {
            String suffix = getSuffix(FILE_PREFIX, node);

            //System.out.println("constructing a " + suffix + " test");

            //cons.constructScalar((ScalarNode) node);
            // TODO match suffix to a file type and call constructor
            return suffix;
        }
    }

    private static String getSuffix(String prefix, Node n) {
        return n.getTag().getValue().substring(prefix.length());
    }

    public SocratesConstructor() {
        this.yamlConstructors.put(new Tag(GROUP_TAG), new GroupConstruct(this));
        this.yamlMultiConstructors.put(FILE_PREFIX, new FileConstruct(this));
        this.yamlMultiConstructors.put(TEST_PREFIX, new TestConstruct(this));
    }
}