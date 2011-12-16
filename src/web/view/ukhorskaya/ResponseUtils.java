package web.view.ukhorskaya;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import web.view.ukhorskaya.session.SessionInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/27/11
 * Time: 5:50 PM
 */

public class ResponseUtils {
    public static String escapeString(String string) {
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
        return string;
    }

    //Get Color as String
    public static String getColor(Color color) {
        java.util.List<String> colors = new ArrayList<String>();
        colors.add(Long.toHexString(color.getRed()));
        colors.add(Long.toHexString(color.getGreen()));
        colors.add(Long.toHexString(color.getBlue()));

        StringBuilder buffer = new StringBuilder("#");
        for (String c : colors) {
            if (c.length() == 1) {
                buffer.append("0");
            }
            buffer.append(c);
        }
        return (buffer.toString());
    }

    //Get fontType as String
    public static String getFontType(int fontType) {
        switch (fontType) {
            case 1:
                return "font-weight: bold;";
            case 2:
                return "font-style: italic;";
            case 3:
                return "font-style: italic; font-weight: bold;";
            default:
                return "";

        }
    }

    public static String substringAfter(String str, String before) {
        int pos = str.indexOf(before);
        if (pos != -1) {
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
            substringBefore(str, after);
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
            ErrorWriter.ERROR_WRITER.writeException(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath()
            ));
            return null;
        } catch (ParserConfigurationException e) {
            ErrorWriter.ERROR_WRITER.writeException(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath()
            ));
            return null;
        } catch (SAXException e) {
            ErrorWriter.ERROR_WRITER.writeException(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath()
            ));
            return null;
        }
        document.getDocumentElement().normalize();
        return document;
    }

}
