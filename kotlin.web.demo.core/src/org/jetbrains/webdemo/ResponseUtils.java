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

package org.jetbrains.webdemo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.session.SessionInfo;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

public class ResponseUtils {
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
        }
        else {
            return "";
        }
        int sPos = str.indexOf(after);
        if (sPos != -1) {
            return str.substring(0, sPos);
        }
        else {
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
        return "[{\"exception\":\"" + error + "\",\"type\":\"err\"}]";
    }

    public static String getErrorWithStackTraceInJson(String error, String stackTrace) {
        return "[{\"exception\":\"" + error + "\",\"type\":\"err\"}, {\"exception\":\"" + stackTrace + "\",\"type\":\"out\"}]";
    }

    public static String getJsonString(String type, String text) {
        return "[{\"type\":\"" + type + "\",\"text\":\"" + text + "\"}]";
    }

    public static String getJsonString(String type, String text, String args) {
        return "[{\"type\":\"" + type + "\",\"text\":\"" + text + "\",\"args\":\"" + args + "\"}]";
    }

    public static String getDate(Calendar calendar) {
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.get(Calendar.MONTH) + 1);
        builder.append("/");
        builder.append(calendar.get(Calendar.DAY_OF_MONTH));
        builder.append("/");
        builder.append(calendar.get(Calendar.YEAR));
        return builder.toString();
    }

    public static String getTime(Calendar calendar) {
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.get(Calendar.HOUR_OF_DAY));
        builder.append(":");
        builder.append(calendar.get(Calendar.MINUTE));
        builder.append(":");
        builder.append(calendar.get(Calendar.SECOND));
        return builder.toString();
    }

    @Nullable
    public static Document getXmlDocument(File file) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document document;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(file);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "unknown", file.getAbsolutePath());
            return null;
        } catch (ParserConfigurationException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "unknown", file.getAbsolutePath());
            return null;
        } catch (SAXException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "unknown", file.getAbsolutePath());
            return null;
        }
        document.getDocumentElement().normalize();
        return document;
    }

    @Nullable
    public static Document getXmlDocument(InputStream is) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document document;
        try {
            dBuilder = dbFactory.newDocumentBuilder();

            document = dBuilder.parse(is);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "unknown", "");
            return null;
        } catch (ParserConfigurationException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "unknown", "");
            return null;
        } catch (SAXException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "unknown", "");
            return null;
        }
        document.getDocumentElement().normalize();
        return document;
    }

    public static String getExampleOrProgramNameByUrl(String url) {
        return ResponseUtils.substringAfter(url, "&name=").replaceAll("%20", " ");
    }

    @NotNull
    public static String[] splitArguments(@NotNull String arguments) {
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
