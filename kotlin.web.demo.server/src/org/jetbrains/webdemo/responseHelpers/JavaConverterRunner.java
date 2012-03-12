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

package org.jetbrains.webdemo.responseHelpers;

import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/19/11
 * Time: 5:46 PM
 */
public class JavaConverterRunner {

    private final String arguments;
    private final String textFromFile;

    private final SessionInfo sessionInfo;

    private volatile boolean isTimeoutException = false;

    public JavaConverterRunner(String text, String arguments, SessionInfo info) {
        this.arguments = arguments;
        this.textFromFile = text;
        this.sessionInfo = info;
    }

    public String getResult() {
        String commandString = generateCommandString();
        Process process;
        sessionInfo.getTimeManager().saveCurrentTime();
        try {
            process = Runtime.getRuntime().exec(commandString);
            process.getOutputStream().close();
        } catch (IOException e) {
            return ResponseUtils.getErrorInJson("Impossible to run your program: IOException handled until execution");
        }

        final StringBuilder errStream = new StringBuilder();
        final StringBuilder outStream = new StringBuilder();

        final Process finalProcess = process;
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isTimeoutException = true;
                finalProcess.destroy();
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                        sessionInfo.getId(), "Timeout exception."));
                errStream.append("Program was terminated after " + Integer.parseInt(ApplicationSettings.TIMEOUT_FOR_EXECUTION) / 1000 + "s.");
            }
        }, Integer.parseInt(ApplicationSettings.TIMEOUT_FOR_EXECUTION));

        final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // Thread that reads std out and feeds the writer given in input
        new Thread() {
            @Override
            public void run() {
                String line;
                try {
                    while ((line = stdOut.readLine()) != null) {
                        outStream.append(ResponseUtils.escapeString(line)).append(ResponseUtils.addNewLine());
                    }
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }.start(); // Starts now

        // Thread that reads std err and feeds the writer given in input
        new Thread() {
            @Override
            public void run() {
                String line;
                try {
                    while ((line = stdErr.readLine()) != null) {
                        errStream.append(ResponseUtils.escapeString(line)).append(ResponseUtils.addNewLine());
                    }
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }.start(); // Starts now

        int exitValue;
        try {
            exitValue = process.waitFor();
        } catch (InterruptedException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    sessionInfo.getType(), textFromFile);
            return ResponseUtils.getErrorInJson("Impossible to run your program: InterruptedException handled.");
        }
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                sessionInfo.getId(), "RunUserProgram " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()
                + " timeout=" + isTimeoutException
                + " commandString=" + commandString));

        if ((exitValue == 1) && !isTimeoutException) {
            if (outStream.length() > 0) {
                if (outStream.indexOf("An error report file with more information is saved as:") != -1) {
                    outStream.delete(0, outStream.length());
                    errStream.append(ApplicationSettings.KOTLIN_ERROR_MESSAGE);
                    String linkForLog = getLinkForLog(outStream.toString());
                }
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("EMPTY", outStream.toString().replaceAll("<br/>", "\n"),
                        sessionInfo.getType(), textFromFile);
            }
        }

        JSONArray jsonArray = new JSONArray();

        if (!isTimeoutException) {
            Map<String, String> mapOut = new HashMap<String, String>();
            mapOut.put("type", "out");
            mapOut.put("text", outStream.toString());
            jsonArray.put(mapOut);
        }

        if (errStream.length() > 0) {
            Map<String, String> mapErr = new HashMap<String, String>();
            if (isKotlinLibraryException(errStream.toString())) {
                writeErrStreamToLog(errStream.toString());
                Map<String, String> map = new HashMap<String, String>();
                map.put("type", "err");
                map.put("text", ApplicationSettings.KOTLIN_ERROR_MESSAGE);
                jsonArray.put(map);
                mapErr.put("type", "out");
            } else {
                ErrorWriterOnServer.LOG_FOR_INFO.error(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                        sessionInfo.getId(), "error while excecution: " + errStream));
                mapErr.put("type", "err");
            }
            mapErr.put("text", errStream.toString());
            jsonArray.put(mapErr);
        }


        timer.cancel();
        return jsonArray.toString();
    }

    private String getLinkForLog(String outStream) {
        String path = ResponseUtils.substringAfter(outStream, "An error report file with more information is saved as:" + ResponseUtils.addNewLine() + "# ");
        path = ResponseUtils.substringBefore(path, ResponseUtils.addNewLine() + "#");
        File log = new File(path);
        FileReader reader = null;
        try {
            reader = new FileReader(log);
            String response = ResponseUtils.readData(reader, true);
            log.delete();
            return response;
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    sessionInfo.getType(), log.getAbsolutePath());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        sessionInfo.getType(), outStream);
            }
        }
        return "";
    }

    private void writeErrStreamToLog(String errStream) {
        int pos = errStream.indexOf(":");
        String stackTrace = "";
        String message = errStream;
        if (pos != -1) {
            message = errStream.substring(0, pos);
            stackTrace = errStream.substring(pos).replaceAll("<br/>", "\n");
        }
        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(message, stackTrace,
                sessionInfo.getType(), textFromFile);
    }

    private boolean isKotlinLibraryException(String str) {
        if (str.contains("LinkageError")
                || str.contains("ClassFormatError")
                || str.contains("UnsupportedClassVersionError")
                || str.contains("GenericSignatureFormatError")
                || str.contains("ExceptionInInitializerError")
                || str.contains("NoClassDefFoundError")
                || str.contains("IncompatibleClassChangeError")
                || str.contains("InstantiationError")
                || str.contains("AbstractMethodError")
                || str.contains("NoSuchFieldError")
                || str.contains("IllegalAccessError")
                || str.contains("NoSuchMethodError")
                || str.contains("VerifyError")
                || str.contains("ClassCircularityError")
                || str.contains("UnsatisfiedLinkError")) {
            return true;
        }
        return false;
    }

    private String generateCommandString() {
        StringBuilder builder = new StringBuilder("java ");
        builder.append("-jar ");
        builder.append("aaa.jar");
        builder.append(" ");
        builder.append(arguments);
        return builder.toString();
    }

}
