package io.breen.socrates.constructor;

import io.breen.socrates.*;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.criteria.DueDate;
import io.breen.socrates.file.File;
import io.breen.socrates.test.Test;
import io.breen.socrates.test.TestGroup;
import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.nodes.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

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

        public Object construct(Node node) {
            Map<Object, Object> map = cons.constructMapping((MappingNode)node);

            List<Test> members = (List<Test>)map.get("members");

            if (members == null)
                throw new InvalidCriteriaException(node.getStartMark(),
                                                   "test group cannot be empty");

            int maxNum = (Integer)map.getOrDefault("max", members.size());
            Integer maxValue = (Integer)map.get("max_value");

            if (maxValue != null && maxValue < 0)
                throw new InvalidCriteriaException(node.getStartMark(),
                                                   "'max_value' cannot be negative");

            if (maxValue == null) {
                maxValue = -1;
            }

            if (maxNum > members.size()) {
                throw new InvalidCriteriaException(node.getStartMark(),
                                                   "maximum cannot be greater than the" +
                                                       " number of tests in the group");
            }

            try {
                return new TestGroup(members, maxNum, maxValue);
            } catch (IllegalArgumentException e) {
                throw new InvalidCriteriaException(node.getStartMark(), e.getMessage());
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

        public Object construct(Node node) {
            String suffix = getSuffix(FILE_PREFIX, node);

            Map<Object, Object> map = null;
            try {
                map = cons.constructMapping((MappingNode)node);
            } catch (ClassCastException e) {
                String msg = "invalid file: should be a mapping";
                throw new InvalidCriteriaException(node.getStartMark(), msg);
            }

            String path = (String)map.get("path");

            if (path == null)
                throw new InvalidCriteriaException(node.getStartMark(),
                                                   "file must have 'path'");

            Double pointValue = (Double)map.get("point_value");

            List<Object> tests = (List<Object>)map.get("tests");

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

    /**
     * Class that constructs the root object of the YAML file --- the Criteria object.
     */
    private class RootConstruct extends AbstractConstruct {
        private SocratesConstructor cons;

        public RootConstruct(SocratesConstructor cons) {
            this.cons = cons;
        }

        public Object construct(Node node) {
            Map<Object, Object> map = null;
            try {
                map = cons.constructMapping((MappingNode)node);
            } catch (ClassCastException e) {
                String msg = "invalid top-level: should be a mapping";
                throw new InvalidCriteriaException(node.getStartMark(), msg);
            }

            String name = (String)map.get("name");
            String id = (String)map.get("id");

            String group = (String)map.get("group");

            Map<DueDate, Double> dueDates = null;

            Map<Date, Double> datesMap = (Map<Date, Double>)map.get("due_dates");

            if (datesMap != null) {
                dueDates = new TreeMap<DueDate, Double>();
                ZoneId thisZone = ZoneId.of(Globals.properties.getProperty("timezone"));

                for (Map.Entry<Date, Double> entry : datesMap.entrySet()) {
                    Date d = entry.getKey();
                    LocalDateTime ldt = LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
                    ZonedDateTime zdt = ldt.atZone(thisZone);

                    DueDate dd = new DueDate(zdt);
                    dueDates.put(dd, entry.getValue());
                }
            }

            List<File> files = (List)map.get("files");

            return new Criteria(name, id, group, dueDates, files);
        }
    }

    public SocratesConstructor() {
        this.rootTag = new Tag(Criteria.class);
        this.yamlConstructors.put(this.rootTag, new RootConstruct(this));

        this.yamlConstructors.put(new Tag(GROUP_TAG), new GroupConstruct(this));
        this.yamlMultiConstructors.put(FILE_PREFIX, new FileConstruct(this));
        this.yamlMultiConstructors.put(TEST_PREFIX, new TestConstruct(this));
    }
}