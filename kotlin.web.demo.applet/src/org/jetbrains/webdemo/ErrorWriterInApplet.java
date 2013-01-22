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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
                                             