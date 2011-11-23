package web.view.ukhorskaya;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/9/11
 * Time: 2:16 PM
 */

public class ErrorsWriter {

    public static final Logger LOG_FOR_EXCEPTIONS = Logger.getLogger("exceptionLogger");
    
    public static void writeErrorToConsole(String message) {
        System.err.println(message);
    }

    public static void writeExceptionToConsole(String message, Throwable e) {
        System.err.println(message);
        e.printStackTrace();
    }

    public static void writeExceptionToConsole(Throwable e) {
        e.printStackTrace();
    }

    public static void writeInfoToConsole(String message) {
        System.out.println(message);
    }

    public static String getExceptionForLog(String typeOfRequest, Throwable throwable, String moreinfo) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n<error>");
        builder.append("\n<type>");
        builder.append(typeOfRequest);
        builder.append("</type>");
        builder.append("\n<message>");
        builder.append(throwable.getMessage());
        builder.append("</message>");
        builder.append("\n<stack>");
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        builder.append(stringWriter.toString());
        builder.append("\n</stack>");
        builder.append("\n<moreinfo>");
        builder.append("\n").append(moreinfo);
        builder.append("\n</moreinfo>");
        builder.append("\n</error>");
        return builder.toString();
    }

    public static String getExceptionForLog(String typeOfRequest, String message, String moreinfo) {
            StringBuilder builder = new StringBuilder();
            builder.append("<error>");
            builder.append("<type>");
            builder.append(typeOfRequest);
            builder.append("</type>");
            builder.append("<message>");
            builder.append(message);
            builder.append("</message>");
            builder.append("<moreinfo>");
            builder.append(moreinfo);
            builder.append("</moreinfo>");
            builder.append("</error>");
            return builder.toString();
        }

}
