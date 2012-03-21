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

import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 12:38 PM
 */

public class JavaRunner {
//    private final Logger LOG = Logger.getLogger(JavaRunner.class);

    private final List<String> files;
    private String arguments;
    private final JSONArray jsonArray;
    private final String textFromFile;

    private final SessionInfo sessionInfo;

    private volatile boolean isTimeoutException = false;

    public JavaRunner(List<String> files, String arguments, JSONArray array, String text, SessionInfo info) {
        this.files = files;
        this.arguments = arguments;
        this.jsonArray = array;
        this.textFromFile = text;
        this.sessionInfo = info;
    }

    public String getResult(String pathToRootOut) {
//        String commandString = generateCommandString(pathToRootOut);
        String[] commandString = generateCommandString(pathToRootOut);
        Process process;
        sessionInfo.getTimeManager().saveCurrentTime();
        try {
            process = Runtime.getRuntime().exec(commandString);
            process.getOutputStream().close();
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    sessionInfo.getType(), Arrays.toString(commandString));
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            return ResponseUtils.getErrorWithStackTraceInJson("Impossible to run your program: IOException handled until execution", stackTrace.toString());
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
                } catch (Throwable e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            sessionInfo.getType(), textFromFile);
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
                } catch (Throwable e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            sessionInfo.getType(), textFromFile);
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
                + " commandString=" + Arrays.toString(commandString)));

        if ((exitValue == 1) && !isTimeoutException) {
            if (outStream.length() > 0) {
                if (outStream.indexOf("An error report file with more information is saved as:") != -1) {
                    outStream.delete(0, outStream.length());
                    errStream.append(ApplicationSettings.KOTLIN_ERROR_MESSAGE);
                    String linkForLog = getLinkForLog(outStream.toString());
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("An error report in JVM", outStream.toString().replace("<br/>", "\n") + "\n" + errStream.toString().replace("<br/>", "\n") + "\n" + linkForLog,
                            sessionInfo.getType(), textFromFile);
                }
            }
        }

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

        for (String fileName : files) {
            deleteFile(fileName, pathToRootOut);
        }

        timer.cancel();
        return jsonArray.toString();
    }

    private String getLinkForLog(String outStream) {
        String path = ResponseUtils.substringAfter(outStream, "An error report file with more information is saved as:" + ResponseUtils.addNewLine() + "# ");
        path = ResponseUtils.substringBefore(path, ResponseUtils.addNewLine() + "#");
        File log = new File(path);
        /*if (log.exists()) {
            String links = ResponseUtils.generateTag("a", "view", "href", "/log=" + log.getAbsolutePath() + "&view");
            links += ResponseUtils.generateTag("a", "download", "href", "/log=" + log.getAbsolutePath() + "&download");
            return links;
        }*/
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
                        sessionInfo.getType(), textFromFile);
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
                || str.contains("VerifyError")
                || str.contains("ClassCircularityError")
                || str.contains("UnsatisfiedLinkError")
                || (str.contains("NoSuchMethodError") && !str.contains("java.lang.NoSuchMethodError: main<br/>"))) {
            return true;
        }
        return false;
    }

    private void readStream(StringBuilder errStream, BufferedReader stdError) throws IOException {
        String tmp;
        while (!isTimeoutException && ((tmp = stdError.readLine()) != null)) {
            errStream.append(ResponseUtils.escapeString(tmp));
            errStream.append(ResponseUtils.addNewLine());
        }
    }

    int returnValue = 0;

    private int tryReadStreams(StringBuilder errStream, StringBuilder outStream, InputStream isOut, InputStream isErr) {
        StringWriter stringWriter = new StringWriter();
        int lengthOut = 0;
        int lengthErr = 0;
        byte[] tmp = new byte[20];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if ((lengthOut = isOut.read(tmp)) >= 0) {
                out.write(tmp, 0, lengthOut);
                stringWriter.write(new String(tmp));
                outStream.append(stringWriter.toString());
            } else if (lengthOut == -1) {
                returnValue--;
            }
            if ((lengthErr = isErr.read(tmp)) >= 0) {
                out.write(tmp, 0, lengthErr);
                errStream.append(new String(tmp));
            } else if (lengthErr == -1) {
                returnValue--;
            }
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    sessionInfo.getType(), textFromFile);
        }
        return returnValue;
    }

    private void deleteFile(String path, String pathToRootOut) {
        File f = new File(pathToRootOut + File.separatorChar + path);
        File parent = f.getParentFile();
        while (!parent.getAbsolutePath().equals(pathToRootOut)) {
            f = parent;
            parent = parent.getParentFile();
        }
        delete(f);
        delete(new File(pathToRootOut));
    }

    private void delete(File file) {
        if (file.isDirectory() && file.exists()) {
            if (file.list().length == 0) {
                if (file.exists()) {
                    file.delete();
                    ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                            sessionInfo.getId(), "Directory is deleted : " + file.getAbsolutePath()));
                }
            } else {
                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    if (file.exists()) {
                        file.delete();
                        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                                sessionInfo.getId(), "Directory is deleted : " + file.getAbsolutePath()));
                    }
                }
            }
        } else {
            if (file.exists()) {
                file.delete();
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                        sessionInfo.getId(), "File is deleted : " + file.getAbsolutePath()));
            }
        }
    }

    private String[] generateCommandString(String pathToRootOut) {
        String[] argsArray = splitArguments();

        String[] builder;
        if (arguments.isEmpty()) {
            builder = new String[5];
        } else {
            builder = new String[argsArray.length + 5];
        }
        builder[0] = ApplicationSettings.JAVA_EXECUTE;
        builder[1] = "-classpath";
        builder[2] = pathToRootOut + File.pathSeparator + ApplicationSettings.KOTLIN_LIB;
        builder[3] = "-Djava.security.manager";
        builder[4] = modifyClassNameFromPath(files.get(0));

        if (!arguments.isEmpty()) {
            System.arraycopy(argsArray, 0, builder, 5, argsArray.length);
        }
        return builder;

    }

    public String[] splitArguments() {
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

    private String modifyArguments(String arguments) {
        return StringEscapeUtils.unescapeJavaScript(arguments);
    }

    private String modifyClassNameFromPath(String path) {
        String name = ResponseUtils.substringBefore(path, ".class");
        name = name.replaceAll("/", ".");
        return name;
    }
}
