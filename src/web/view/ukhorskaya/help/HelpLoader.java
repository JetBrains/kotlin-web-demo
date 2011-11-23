package web.view.ukhorskaya.help;

import org.json.JSONArray;
import org.w3c.dom.*;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.server.ServerSettings;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/22/11
 * Time: 6:33 PM
 */

public class HelpLoader {
    private static HelpLoader helpLoader = new HelpLoader();

    private HelpLoader() {
        generateHelpForExamples();
        generateHelpForWords();
    }

    private JSONArray resultExamples;
    private JSONArray resultWords;

    public static HelpLoader getInstance() {
        return helpLoader;
    }

    public String getHelpForExamples() {
        return resultExamples.toString();
    }

    public String getHelpForWords() {
        return resultWords.toString();
    }

    private String getTagValueWithInnerTags(String tag, Element element) {
        StringBuilder result = new StringBuilder();
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.append(getTagValueWithTagName(nodeList.item(i)));
        }
        return result.toString();
    }

    private String getTagValueWithTagName(Node node) {
        StringBuilder result = new StringBuilder();

        if (node.getNodeType() == 3) {
            result.append(node.getNodeValue());
        } else {
            result.append("<");
            result.append(node.getNodeName());
            if (node.getNodeName().equals("a")) {
                result.append(" target=\"_blank\" ");
            }
            if (node.hasAttributes()) {
                result.append(" ");
                NamedNodeMap map = node.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    result.append(map.item(i).getNodeName());
                    result.append("=\"");
                    result.append(map.item(i).getTextContent());
                    result.append("\" ");
                }
            }
            result.append(">");
            if (node.hasChildNodes()) {
                NodeList nodeList = node.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    result.append(getTagValueWithTagName(nodeList.item(i)));
                }
            }
            result.append("</");
            result.append(node.getNodeName());
            result.append(">");
        }
        return result.toString();
    }

    private String getTagValue(String sTag, Element element) {
        NodeList nodeList = element.getElementsByTagName(sTag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        if (node != null) {
            return node.getNodeValue();
        } else {
            return "";
        }
    }

    public static void updateExamplesHelp() {
        HelpLoader.getInstance().generateHelpForExamples();
        HelpLoader.getInstance().generateHelpForWords();
    }

    private void generateHelpForExamples() {
        resultWords = new JSONArray();
        try {
            File file = new File(ServerSettings.HELP_ROOT + File.separator + ServerSettings.HELP_FOR_WORDS);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("keyword");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("name", getTagValue("name", element));
                    map.put("text", getTagValueWithInnerTags("text", element));
                    resultWords.put(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorsWriter.writeInfoToConsole("Help for keywords was loaded.");
    }

    private void generateHelpForWords() {
        resultExamples = new JSONArray();
        try {
            File file = new File(ServerSettings.EXAMPLES_ROOT + File.separator + ServerSettings.HELP_FOR_EXAMPLES);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("example");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("name", getTagValue("name", element));
                    map.put("text", getTagValueWithInnerTags("text", element));
                    map.put("args", getTagValue("args", element));
                    resultExamples.put(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorsWriter.writeInfoToConsole("Help for examples was loaded.");
    }
}
