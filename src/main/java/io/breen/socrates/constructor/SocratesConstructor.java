package io.breen.socrates.constructor;

import io.breen.socrates.*;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.criteria.DueDate;
import io.breen.socrates.file.File;
import io.breen.socrates.immutable.test.*;
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

        public Object construct(Node anyNode) {
            MappingNode node = null;
            try {
                node = (MappingNode)anyNode;
            } catch (ClassCastException e) {
                String msg = "invalid file: should be a mapping";
                throw new InvalidCriteriaException(anyNode.getStartMark(), msg);
            }

            Map<Object, Object> map = cons.constructMapping(node);

            List<Test> members = (List<Test>)map.get("members");

            if (members == null)
                throw new InvalidCriteriaException(anyNode.getStartMark(),
                                                   "test group cannot be empty");

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
                throw new InvalidCriteriaException(anyNode.getStartMark(), e.getMessage());
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

            String path = (String)map.get("path");

            if (path == null)
                throw new InvalidCriteriaException(anyNode.getStartMark(),
                                                   "file must have 'path'");

            Double pointValue = coerceToDouble(map.get("point_value"));

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

            String name = (String)map.get("name");
            String id = (String)map.get("id");

            String group = (String)map.get("group");

            Map<DueDate, Double> dueDates = null;

            Map<Date, Double> datesMap = (Map<Date, Double>)map.get("due_dates");

            if (datesMap != null) {
                dueDates = new TreeMap<>();
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

    private static String getSuffix(String prefix, Node n) {
        // TODO check that tag even exists?
        return n.getTag().getValue().substring(prefix.length());
    }

    static Double coerceToDouble(Object obj) {
        if (obj instanceof Integer)
            return ((Integer)obj).doubleValue();

        if (obj instanceof Double)
            return (Double)obj;

        if (obj == null)
            return null;

        throw new ClassCastException(obj + " is not coercible to Double");
    }
}
