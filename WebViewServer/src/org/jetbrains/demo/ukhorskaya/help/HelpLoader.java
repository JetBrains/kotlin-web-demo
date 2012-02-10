package org.jetbrains.demo.ukhorskaya.help;

import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.json.JSONArray;
import org.w3c.dom.*;

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
    
    private static StringBuilder response;

    private HelpLoader() {
        response = new StringBuilder();
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

    public static String updateExamplesHelp() {
        response = new StringBuilder();
        HelpLoader.getInstance().generateHelpForExamples();
        HelpLoader.getInstance().generateHelpForWords();
        return response.toString();
    }

    private void generateHelpForExamples() {
        resultWords = new JSONArray();
        try {
            File file = new File(ServerSettings.HELP_ROOT + File.separator + ServerSettings.HELP_FOR_WORDS);
            Document doc = ResponseUtils.getXmlDocument(file);
            if (doc == null) {
                return;
            }
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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), "");
        }
        ErrorWriter.writeInfoToConsole("Help for keywords was loaded.");
        response.append("\nHelp for keywords was loaded.");
    }

    private void generateHelpForWords() {
        resultExamples = new JSONArray();
        try {
            File file = new File(ServerSettings.EXAMPLES_ROOT + File.separator + ServerSettings.HELP_FOR_EXAMPLES);
            Document doc = ResponseUtils.getXmlDocument(file);
            if (doc == null) {
                return;
            }

            NodeList nodeList = doc.getElementsByTagName("example");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("name", getTagValue("name", element));
                    map.put("text", getTagValueWithInnerTags("text", element));
                    map.put("args", getTagValue("args", element));
                    map.put("mode", getTagValue("mode", element));
                    resultExamples.put(map);
                }
            }
        } catch (Exception e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), "");
        }
        ErrorWriter.writeInfoToConsole("Help for examples was loaded.");
        response.append("\nHelp for examples was loaded.");
    }
}
