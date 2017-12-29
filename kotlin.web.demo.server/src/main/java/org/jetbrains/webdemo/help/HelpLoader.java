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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.CommonSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.session.SessionInfo;
import org.w3c.dom.*;

import java.io.File;

public class HelpLoader {
    private static HelpLoader helpLoader = new HelpLoader();

    private static StringBuilder response;
    private ArrayNode resultWords;

    private HelpLoader() {
        response = new StringBuilder();
        generateHelpForWords();
    }

    public static HelpLoader getInstance() {
        return helpLoader;
    }

    public static String updateExamplesHelp() {
        response = new StringBuilder();
        HelpLoader.getInstance().generateHelpForWords();
        return response.toString();
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

    private void generateHelpForWords() {
        resultWords = new ArrayNode(JsonNodeFactory.instance);
        try {
            File file = new File(CommonSettings.HELP_DIRECTORY + File.separator + ApplicationSettings.HELP_FOR_WORDS);
            Document doc = ResponseUtils.getXmlDocument(file);
            if (doc == null) {
                return;
            }
            NodeList nodeList = doc.getElementsByTagName("keyword");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    ObjectNode jsonObject = resultWords.addObject();
                    jsonObject.put("word", getTagValue("name", element));
                    jsonObject.put("help", getTagValueWithInnerTags("text", element));
                }
            }
        } catch (Exception e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), "unknown", "");
        }
        response.append("\nHelp for keywords was loaded.");
    }
}
