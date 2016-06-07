/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package org.jetbrains.webdemo;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ResponseUtils {
    private static final List<String> URLS = new ArrayList<String>();

    static {
        URLS.add("http://unit-304.labs.intellij.net:8080");
        URLS.add("http://local.hadihariri.com:4000");
        URLS.add("http://hhariri.github.io/tests");
        URLS.add("http://hhariri.github.io");
        URLS.add("http://kotlin-demo.jetbrains.com");
        URLS.add("http://jetbrains.github.io/kotlin-web-site");
        URLS.add("http://jetbrains.github.io/kotlin-fiddler");
        URLS.add("http://jetbrains.github.io");
    }

    public static String escapeString(String string) {
        if (string != null && !string.isEmpty()) {
            if (string.contains("<")) {
                string = string.replaceAll("<", "&lt;");
            }
            if (string.contains(">")) {
                string = string.replaceAll(">", "&gt;");
            }
            if (string.contains("&")) {
                string = string.replaceAll("&", "&amp;");
            }
            /* if (string.contains("\"")) {
                string = string.replaceAll("\"", "'");
            }*/
        }
        return string;
    }

    public static String generateRequestString(String type, String args) {
        return "/kotlinServer?sessionId=-1&type=" + type + "&args=" + args;
    }

    public static String substringAfter(String str, String before) {
        int pos = str.indexOf(before);
        if (pos != -1) {
            if (pos == str.length()) {
                return "";
            }
            return str.substring(pos + before.length());
        }
        return "";
    }

    //Substring str and return all str if before string not found
    public static String substringAfterReturnAll(String str, String before) {
        int pos = str.indexOf(before);
        if (pos != -1) {
            return str.substring(pos + before.length());
        }
        return str;
    }

    public static String substringBefore(String str, String after) {
        int pos = str.indexOf(after);
        if (pos != -1) {
            return str.substring(0, pos);
        }
        return str;
    }

    public static String substringBetween(String str, String before, String after) {
        int fPos = str.indexOf(before);
        if (fPos != -1) {
            str = str.substring(fPos + before.length());
        } else {
            return "";
        }
        int sPos = str.indexOf(after);
        if (sPos != -1) {
            return str.substring(0, sPos);
        } else {
            return str;
        }
    }

    public static String addNewLine() {
        return "<br/>";
    }

    public static String readData(InputStream is) throws IOException {
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder response = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);

        String tmp;
        while ((tmp = bufferedReader.readLine()) != null) {
            response.append(tmp);
        }

        reader.close();
        return response.toString();
    }

    public static String readData(Reader reader, boolean addNewLine) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);

        String tmp;
        while ((tmp = bufferedReader.readLine()) != null) {
            response.append(tmp);
            if (addNewLine) {
                response.append("\n");
            }
        }
        bufferedReader.close();
        return response.toString();
    }

    public static String readData(InputStream is, boolean addNewLine) throws IOException {
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder response = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);

        String tmp;
        while ((tmp = bufferedReader.readLine()) != null) {
            response.append(tmp);
            if (addNewLine) {
                response.append("\n");
            }
        }
        reader.close();
        return response.toString();
    }

    public static String generateTag(String tagName, String content) {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(tagName);
        builder.append(">");
        builder.append(content);
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
        return builder.toString();

    }

    public static String generateTag(String tagName, String content, String attrName, String attrValue) {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(tagName);
        builder.append(" ");
        builder.append(attrName);
        builder.append("=\"");
        builder.append(attrValue);
        builder.append("\" class=\"internal\"");
//        builder.append("\" title=\"En/Dehydra\" class=\"internal\"");
        builder.append(">");
        builder.append(content);
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
        return builder.toString();

    }

    public static String getErrorInJson(String error) {
        return "[{\"text\":\"" + error + "\",\"type\":\"err\"}]";
    }

    public static ObjectNode getErrorAsJsonNode(String error) {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.put("type", "err");
        result.put("exception", error);
        return result;
    }

    public static String getErrorWithStackTraceInJson(String error, String stackTrace) {
        return "[{\"text\":\"" + error + "\",\"stackTrace\":\"" + stackTrace + "\",\"type\":\"err\"}]";
    }

    public static ObjectNode getErrorWithStackTraceAsJsonNode(String error, String stackTrace) {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.put("type", "err");
        result.put("exception", error);
        result.put("stackTrace", stackTrace);
        return result;
    }

    public static Document getXmlDocument(File file) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document document;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(file);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "ANALYZE_LOG", "unknown", file.getAbsolutePath());
            return null;
        } catch (ParserConfigurationException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "ANALYZE_LOG", "unknown", file.getAbsolutePath());
            return null;
        } catch (SAXException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "ANALYZE_LOG", "unknown", file.getAbsolutePath());
            return null;
        }
        document.getDocumentElement().normalize();
        return document;
    }

    public static String escapeURL(String input) {
        return input.replaceAll(" ", "%20");
    }

    public static String unEscapeURL(String input) {
        return input.replaceAll("%20", " ");
    }

    public static String getExampleOrProgramNameByUrl(String url) {
        return ResponseUtils.substringAfter(url, "&name=").replaceAll("%20", " ");
    }

    public static String getExampleFolderByUrl(String url) {
        return ResponseUtils.substringBefore(url, "&name").replaceAll("%20", " ");
    }

    public static String[] splitArguments(String arguments) {
        boolean inQuotes = false;
        ArrayList<String> arrayList = new ArrayList<String>();
        int firstChar = 0;
        int i;
        char ch;
        for (i = 0; i < arguments.length(); i++) {
            ch = arguments.charAt(i);
            if (ch == '\\' && arguments.charAt(i + 1) == '\"') {
                i++;
                continue;
            }
            if (ch == '\"') {
                inQuotes = !inQuotes;
            }
            if (ch == ' ') {
                if (!inQuotes) {
                    arrayList.add(arguments.substring(firstChar, i));
                    firstChar = i + 1;
                }
            }
        }

        if (firstChar != arguments.length()) {
            arrayList.add(arguments.substring(firstChar, arguments.length()));
        }

        String[] result = new String[arrayList.size()];

        int j = 0;
        for (String element : arrayList) {
            element = element.replaceAll("\\\\\"", "QUOTE");
            element = element.replaceAll("\"", "");
            element = element.replaceAll("QUOTE", "\\\\\"");
            result[j] = element;
            j++;
        }
        return result;
    }
}
