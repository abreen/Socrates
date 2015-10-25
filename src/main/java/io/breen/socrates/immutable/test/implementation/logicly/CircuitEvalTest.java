package io.breen.socrates.immutable.test.implementation.logicly;


import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.logicly.LogiclyFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.util.Pair;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CircuitEvalTest extends Test implements Automatable<LogiclyFile> {

    private static final int MAX_FILE_SIZE = 1 << 16;
    private static final XPath xpInstance;
    private static Logger logger = Logger.getLogger(CircuitEvalTest.class.getName());
    private static XPathExpression objectsExpr;
    private static XPathExpression connectionsExpr;
    private static XPathExpression locationLeftExpr;
    private static XPathExpression locationRightExpr;
    private static DocumentBuilder documentBuilder;

    static {
        xpInstance = XPathFactory.newInstance().newXPath();

        try {
            objectsExpr = xpInstance.compile("logicly/object");
            connectionsExpr = xpInstance.compile("logicly/connection");
            locationLeftExpr = xpInstance.compile("logicly/location[@id='left']");
            locationRightExpr = xpInstance.compile("logicly/location[@id='right']");

        } catch (XPathExpressionException x) {
            logger.severe("error compiling built-in XPath expression: " + x);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            documentBuilder = factory.newDocumentBuilder();

        } catch (ParserConfigurationException x) {
            logger.severe("error creating DocumentBuilder");
        }
    }

    /**
     * For each switch, the on/off state for each switch.
     */
    public Map<String, Boolean> input;

    /**
     * For each light bulb that should be checked, the on/off state that the bulb should take on.
     */
    public Map<String, Boolean> output;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public CircuitEvalTest() {}

    public CircuitEvalTest(double deduction, String description) {
        super(deduction, description);
    }

    @Override
    public String toString() {
        return "CircuitEvalTest(" +
                "output=" + output + ", " +
                "input=" + input + ")";
    }

    @Override
    public String getTestTypeName() {
        return "circuit evaluation";
    }

    @Override
    public boolean shouldPass(LogiclyFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, javax.swing.text.Document transcript)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        List<Switch> switches;
        List<LightBulb> lightBulbs;

        try {
            org.w3c.dom.Document doc = loadXML(target.fullPath);

            Pair<List<Switch>, List<LightBulb>> p = build(doc);
            switches = p.first;
            lightBulbs = p.second;

            HashMap<String, Switch> switchMap = new HashMap<>();
            HashMap<String, LightBulb> lightBulbMap = new HashMap<>();

            for (Switch s : switches)
                switchMap.put(s.exportName, s);

            for (LightBulb b : lightBulbs)
                lightBulbMap.put(b.exportName, b);

            // use the map to efficiently set the state of each switch
            for (Map.Entry<String, Boolean> entry : input.entrySet()) {
                Switch s = switchMap.get(entry.getKey());
                s.state = entry.getValue();
            }

            // for each output specified in criteria, evaluate from that light bulb
            for (Map.Entry<String, Boolean> entry : output.entrySet()) {
                String exportName = entry.getKey();
                LightBulb b = lightBulbMap.get(exportName);

                if (b == null) {
                    // there is no light bulb with the proper export name
                    throw new CannotBeAutomatedException(
                            "could not find light bulb with export name '" + exportName + "'"
                    );
                }

                boolean expected = entry.getValue();

                try {
                    if (b.evaluate()[0] != expected) return false;
                } catch (UndeterminedStateException x) {
                    return false;
                }
            }

        } catch (IOException | ParserConfigurationException | SAXException |
                XPathExpressionException | DataFormatException x) {
            throw new AutomationFailureException(x);
        } catch (UnsupportedGateException x) {
            throw new CannotBeAutomatedException("encountered unrecognized gate");
        }

        return true;
    }

    private org.w3c.dom.Document loadXML(Path path)
            throws IOException, DataFormatException, ParserConfigurationException, SAXException
    {
        byte[] compressedBytes = Files.readAllBytes(path);
        Inflater decompresser = new Inflater(true);
        decompresser.setInput(compressedBytes, 0, compressedBytes.length);

        byte[] bytes = new byte[MAX_FILE_SIZE];
        int numBytes = decompresser.inflate(bytes);
        decompresser.end();

        return documentBuilder.parse(new ByteArrayInputStream(bytes, 0, numBytes));
    }

    private Pair<List<Switch>, List<LightBulb>> build(Node root)
            throws XPathExpressionException, UnsupportedGateException
    {
        HashMap<String, Evaluatable> objects = new HashMap<>();
        HashMap<String, Switch> switches = new HashMap<>();
        HashMap<String, LightBulb> lightBulbs = new HashMap<>();

        NodeList nodes;

        /*
         * Add the objects from this circuit to the object set. If an object's type is not a
         * built-in Logicly type, then it refers to a custom definition. A new CustomCircuit
         * object is created in this case.
         */
        nodes = (NodeList)objectsExpr.evaluate(root, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);

            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element)n;

            Evaluatable obj = null;
            String uid = el.getAttribute("uid");
            String type = el.getAttribute("type");

            if (type.startsWith("switch")) {
                String exportName = el.getAttribute("exportName");
                obj = new Switch(UUID.fromString(uid), exportName);
                switches.put(uid, (Switch)obj);

            } else if (type.startsWith("light_bulb")) {
                String exportName = el.getAttribute("exportName");
                obj = new LightBulb(UUID.fromString(uid), exportName);
                lightBulbs.put(uid, (LightBulb)obj);

            } else if (type.startsWith("not")) {
                obj = new NotGate(UUID.fromString(uid));

            } else if (type.startsWith("and")) {
                String numInputs = el.getAttribute("inputs");
                obj = new AndGate(UUID.fromString(uid), Integer.parseInt(numInputs));

            } else if (type.startsWith("or")) {
                String numInputs = el.getAttribute("inputs");
                obj = new OrGate(UUID.fromString(uid), Integer.parseInt(numInputs));

            } else if (type.startsWith("constant_low")) {
                obj = new LowConstant(UUID.fromString(uid));

            } else if (type.startsWith("label")) {
                // do nothing

            } else {
                /*
                 * If the type is referring to a custom circuit, the type is a UUID and there
                 * should be a <custom> element elsewhere in the top-level of the document that can
                 * be used to construct the circuit. We will build an XPath expression to locate the
                 * <logicly> element containing the definition, and make a recursive call to build
                 * it.
                 */
                try {
                    UUID.fromString(type);
                } catch (IllegalArgumentException x) {
                    throw new UnsupportedGateException();
                }

                String circuitName = el.getAttribute("name");

                String expr = "/logicly/custom[@type='" + type + "']";
                XPathExpression xp = xpInstance.compile(expr);

                Node customRoot = (Node)xp.evaluate(root, XPathConstants.NODE);
                Pair<List<Switch>, List<LightBulb>> circuit = build(customRoot);

                obj = new CustomCircuit(
                        UUID.fromString(uid),
                        circuitName,
                        circuit.first.toArray(new Switch[circuit.first.size()]),
                        circuit.second.toArray(new LightBulb[circuit.second.size()])
                );
            }

            if (obj != null) objects.put(uid, obj);
        }

        /*
         * For each connection, identify the endpoints of the connection using the UUID
         * and update the input list of the object whose input pin is being used.
         */
        nodes = (NodeList)connectionsExpr.evaluate(root, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);

            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element)n;

            String inputUID = el.getAttribute("inputUID");
            int inputIndex = Integer.parseInt(el.getAttribute("inputIndex"));

            String outputUID = el.getAttribute("outputUID");
            int outputIndex = Integer.parseInt(el.getAttribute("outputIndex"));

            Evaluatable from = objects.get(outputUID);
            Evaluatable to = objects.get(inputUID);

            if (from instanceof CustomCircuit) {
                // use the underlying LightBulb as the source of the connection
                CustomCircuit cc = (CustomCircuit)from;
                from = cc.outputs[outputIndex];
                outputIndex = 0;
            }

            if (to instanceof CustomCircuit) {
                // use the underlying Switch as the destination of the connection
                CustomCircuit cc = (CustomCircuit)to;
                to = cc.inputs[inputIndex];
                inputIndex = 0;
            }

            to.inputs[inputIndex] = from.outputs[outputIndex];
        }

        /*
         * Use the <location> elements, if any, to reorder the switch and light bulb lists.
         * This is only used when this method is called recursively to construct custom circuits,
         * and the supercircuit needs to connect objects to this circuit by position, not by the
         * name of a switch/light bulb.
         */
        Element left = (Element)locationLeftExpr.evaluate(root, XPathConstants.NODE);
        Element right = (Element)locationRightExpr.evaluate(root, XPathConstants.NODE);

        List<Switch> switchList;
        List<LightBulb> lightBulbList;

        if (left != null && right != null) {
            switchList = new ArrayList<>(switches.size());
            lightBulbList = new ArrayList<>(lightBulbs.size());

            /*
             * The "left" element contains the ordering of inputs (switches).
             * The "right" element contains the ordering of outputs (light bulbs).
             */
            String[] leftUIDs = left.getAttribute("uids").split(",");
            String[] rightUIDs = right.getAttribute("uids").split(",");

            for (String uid : leftUIDs)
                switchList.add(switches.get(uid));

            for (String uid : rightUIDs)
                lightBulbList.add(lightBulbs.get(uid));

        } else {
            switchList = new ArrayList<>(switches.values());
            lightBulbList = new ArrayList<>(lightBulbs.values());
        }

        return new Pair<>(switchList, lightBulbList);
    }
}
