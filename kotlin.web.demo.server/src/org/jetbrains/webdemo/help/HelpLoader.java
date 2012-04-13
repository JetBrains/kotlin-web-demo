/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo.help;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.examplesLoader.ExampleObject;
import org.jetbrains.webdemo.examplesLoader.ExamplesHolder;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
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
        generateHelpForWords();
        generateHelpForExamples();
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
        HelpLoader.getInstance().generateHelpForWords();
        HelpLoader.getInstance().generateHelpForExamples();
        return response.toString();
    }

    private void generateHelpForWords() {
        resultWords = new JSONArray();
        try {
            File file = new File(ApplicationSettings.HELP_DIRECTORY + File.separator + ApplicationSettings.HELP_FOR_WORDS);
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

    private void generateHelpForExamples() {
        resultExamples = new JSONArray();
        try {
            File file = new File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + ApplicationSettings.HELP_FOR_EXAMPLES);
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
//                    map.put("args", getTagValue("args", element));
//                    map.put("mode", getTagValue("mode", element));

                    ExampleObject example = ExamplesHolder.getExample(map.get("name"));
                    if (example != null) {
                        @Nullable String mode = getTagValue("mode", element);
                        @Nullable String args = getTagValue("args", element);
                        if (mode != null) {
                            example.confType = mode;
                        }
                        if (args != null) {
                            example.args = args;
                        }
                    }

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
