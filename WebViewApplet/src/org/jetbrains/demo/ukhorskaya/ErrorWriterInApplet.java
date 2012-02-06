package org.jetbrains.demo.ukhorskaya;

import org.jetbrains.demo.ukhorskaya.exceptions.KotlinCoreException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/5/11
 * Time: 2:27 PM
 */

public class ErrorWriterInApplet extends ErrorWriter {

    private static final ErrorWriterInApplet writer = new ErrorWriterInApplet();

    public static ErrorWriterInApplet getInstance() {
        return writer;
    }

    private ErrorWriterInApplet() {
    }

    @Override
    public void writeException(String message) {
        sendTextToServer(message, MainApplet.request, "error");
//        System.out.println(message);
    }

    @Override
    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String description) {
        sendTextToServer(ErrorWriter.getExceptionForLog(type, e, description), MainApplet.request, "errorInKotlin");
    }

    @Override
    public void writeExceptionToExceptionAnalyzer(String message, String stackTrace, String type, String description) {
        sendTextToServer(ErrorWriter.getExceptionForLog(type, message, stackTrace, description), MainApplet.request, "errorInKotlin");
    }

    @Override
    public void writeInfo(String message) {
//        sendTextToServer(message, MainApplet.request, "info");
    }

    public static void sendTextToServer(String text, String request, String type) {
        String urlPath = request + ResponseUtils.generateRequestString("writeLog", type);

        URL url;
        try {
            url = new URL(urlPath);

            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) url.openConnection();

            if (text != null) {
                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write("text=" + text);
                wr.flush();
                wr.close();
            }
            urlConnection.connect();
            BufferedReader in;

            try {
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            } catch (Exception e) {
                in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            }

            in.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
                                             