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

package org.jetbrains.webdemo.backend.responseHelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.kotlin.backend.common.output.OutputFile;
import org.jetbrains.kotlin.idea.MainFunctionDetector;
import org.jetbrains.kotlin.load.kotlin.PackageClassUtils;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.BackendSettings;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class JavaRunner {
    private static Timer timer = new Timer(true);

    private final BindingContext bindingContext;
    private final List<OutputFile> files;
    private final ArrayNode jsonArray;
    private final JetFile currentFile;
    private final BackendSessionInfo sessionInfo;
    int returnValue = 0;
    private String arguments;
    private volatile boolean isTimeoutException = false;
    private volatile boolean outputIsTooLong = false;

    public JavaRunner(BindingContext bindingContext, List<OutputFile> files, String arguments, ArrayNode array, JetFile currentFile, BackendSessionInfo info) {
        this.bindingContext = bindingContext;
        this.files = files;
        this.arguments = arguments;
        this.jsonArray = array;
        this.currentFile = currentFile;
        this.sessionInfo = info;
    }

    public String getResult(String pathToRootOut) throws Exception {
        Process process = null;
        try {
            String[] commandString = generateCommandString(pathToRootOut);
            sessionInfo.getTimeManager().saveCurrentTime();
            try {
                process = Runtime.getRuntime().exec(commandString);
                process.getOutputStream().close();
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        sessionInfo.getType(), sessionInfo.getOriginUrl(), Arrays.toString(commandString));
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                return ResponseUtils.getErrorWithStackTraceInJson("Impossible to run your program: IOException handled until execution", stackTrace.toString());
            }

            final StringBuilder errStream = new StringBuilder();
            final StringBuilder outStream = new StringBuilder();

            final Process finalProcess = process;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isTimeoutException = true;
                    finalProcess.destroy();
                }
            }, BackendSettings.TIMEOUT_FOR_EXECUTION);

            final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Thread that reads std out and feeds the writer given in input
            Thread stdReader = new Thread() {
                @Override
                public void run() {
                    String line;
                    try {
                        while (!isTimeoutException &&
                                (line = stdOut.readLine()) != null) {
                            outStream.append(ResponseUtils.escapeString(line));
                            if(outStream.length() > BackendSettings.MAX_OUTPUT_SIZE){
                                outputIsTooLong = true;
                                finalProcess.destroy();
                            }
                        }
                    } catch (Throwable e) {
                        //Stream closes after timeout, and stdOut.readLine can produce "Stream closed exception"
                        if(!isTimeoutException) {
                            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                                    sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
                        }
                    }
                }
            };
            stdReader.start(); // Starts now

            // Thread that reads std err and feeds the writer given in input
            Thread stdErrReader = new Thread() {
                @Override
                public void run() {
                    String line;
                    try {
                        while (!isTimeoutException &&
                                (line = stdErr.readLine()) != null) {
                            errStream.append(ResponseUtils.escapeString(line)).append(ResponseUtils.addNewLine());
                        }
                    } catch (Throwable e) {
                        if(!isTimeoutException) {
                            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                                    sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
                        }
                    }
                }
            };
            stdErrReader.start(); // Starts now

            int exitValue;
            try {
                stdReader.join(10000);
                stdErrReader.join(10000);
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
                return ResponseUtils.getErrorInJson("Impossible to run your program: InterruptedException handled.");
            } finally {
                try {
                    stdOut.close();
                    stdErr.close();
                } catch (IOException e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
                    //noinspection ReturnInsideFinallyBlock
                    return ResponseUtils.getErrorInJson("Couldn't close output stream after executing code");
                }
            }

//            ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
//                    sessionInfo.getId(), "RunUserProgram " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()
//                            + " timeout=" + isTimeoutException
//                            + " commandString=" + Arrays.toString(commandString)));


            if (!isTimeoutException) {
                if ((exitValue == 1) &&
                        !isTimeoutException &&
                        outStream.length() > 0 &&
                        outStream.indexOf("An error report file with more information is saved as:") != -1) {
                    outStream.delete(0, outStream.length());
                    errStream.append(BackendSettings.KOTLIN_ERROR_MESSAGE);
                    String linkForLog = getLinkForLog(outStream.toString());
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("An error report in JVM", outStream.toString().replace("<br/>", "\n") + "\n" + errStream.toString().replace("<br/>", "\n") + "\n" + linkForLog,
                            sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
                } else if (!isTimeoutException) {
                    if (outputIsTooLong) {
                        throw new Exception("Your program produces too much output.");
                    } else {
                        try {
                            if (sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JUNIT)) {
                                ObjectNode output = jsonArray.addObject();
                                ArrayNode executorOutput = (ArrayNode) new ObjectMapper().readTree(outStream.toString());
                                output.put("testResults", executorOutput);
                                output.put("type", "out");
                            } else {
                                ObjectNode output = (ObjectNode) new ObjectMapper().readTree(outStream.toString());
                                output.put("type", "out");
                                jsonArray.add(output);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (errStream.length() > 0) {

                    if (isKotlinLibraryException(errStream.toString())) {
                        writeErrStreamToLog(errStream.toString().replace("\t", "    "));

                        ObjectNode jsonObject = jsonArray.addObject();
                        jsonObject.put("type", "err");
                        jsonObject.put("text", BackendSettings.KOTLIN_ERROR_MESSAGE);
                    } else {
                        ObjectNode errObject = new ObjectNode(JsonNodeFactory.instance);
                        errObject.put("type", "err");
                        errObject.put("text", errStream.toString().replace("\t", "    "));
                        jsonArray.add(errObject);
//                        ErrorWriter.LOG_FOR_INFO.error(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
//                                sessionInfo.getId(), "error while excecution: " + errStream));
                    }

                }
                return jsonArray.toString();
            } else {
                throw new TimeoutException(
                        "Program was terminated after " + BackendSettings.TIMEOUT_FOR_EXECUTION / 1000 + "s.");
            }
        } finally {
            for (OutputFile file : files) {
                deleteFile(file.getRelativePath(), pathToRootOut);
            }
            if(process != null){
                process.destroy();
            }
        }
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
                    sessionInfo.getType(), sessionInfo.getOriginUrl(), log.getAbsolutePath());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
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
                sessionInfo.getType(), sessionInfo.getOriginUrl(), currentFile.getText());
    }

    private boolean isKotlinLibraryException(String str) {
        if (str.contains("LinkageError")
                || str.contains("ClassFormatError")
                || str.contains("UnsupportedClassVersionError")
                || str.contains("GenericSignatureFormatError")
                || str.contains("ExceptionInInitializerError")
                || (str.contains("NoClassDefFoundError") && !str.contains("_DefaultPackage"))
                || str.contains("IncompatibleClassChangeError")
                || str.contains("InstantiationError")
                || str.contains("AbstractMethodError")
                || str.contains("NoSuchFieldError")
                || (str.contains("IllegalAccessError") && !str.contains("kotlin.io.IoPackage"))
                || str.contains("VerifyError")
                || str.contains("ClassCircularityError")
                || str.contains("UnsatisfiedLinkError")
                || (str.contains("NoSuchMethodError") && !str.contains("java.lang.NoSuchMethodError: main<br/>"))) {
            return true;
        }
        return false;
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
//                    ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
//                            sessionInfo.getId(), "Directory is deleted : " + file.getAbsolutePath()));
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
//                        ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
//                                sessionInfo.getId(), "Directory is deleted : " + file.getAbsolutePath()));
                    }
                }
            }
        } else {
            if (file.exists()) {
                file.delete();
//                ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
//                        sessionInfo.getId(), "File is deleted : " + file.getAbsolutePath()));
            }
        }
    }

    private String[] generateCommandString(String pathToRootOut) {
        List<String> argsArray = Arrays.asList(ResponseUtils.splitArguments(arguments));

        List<String> builder;
        if (arguments.isEmpty()) {
            builder = new ArrayList<>(5);
        } else {
            builder = new ArrayList<>(argsArray.size() + 5);
        }
        builder.add(BackendSettings.JAVA_EXECUTE);
        builder.add("-ea");
        builder.add("-Xmx32m");
        builder.add("-Djava.security.manager");
        builder.add("-Djava.security.policy=" + BackendSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy");
        builder.add("-classpath");
        String classpath = (pathToRootOut +
                File.pathSeparator + BackendSettings.KOTLIN_LIBS_DIR + File.separator + "kotlin-runtime.jar" +
                File.pathSeparator + BackendSettings.KOTLIN_LIBS_DIR + File.separator + "kotlin-reflect.jar" +
                File.pathSeparator + BackendSettings.CLASS_PATH + File.separator + "Executors.jar");
        if (sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JUNIT)) {
            builder.add(classpath +
                            File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "junit.jar" +
                            File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "jackson-databind.jar" +
                            File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "jackson-core.jar" +
                            File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "jackson-annotations.jar" +
                            File.pathSeparator + BackendSettings.KOTLIN_LIBS_DIR + File.separator + "kotlin-compiler.jar"
            );
            builder.add("org.jetbrains.webdemo.executors.JunitExecutor");
            builder.add(pathToRootOut);
        } else {
            builder.add(classpath +
                    File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "jackson-databind.jar" +
                    File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "jackson-core.jar" +
                    File.pathSeparator + BackendSettings.LIBS_DIR + File.separator + "jackson-annotations.jar");
            builder.add("org.jetbrains.webdemo.executors.JavaExecutor");
            builder.add(findMainClass());
            if (!arguments.isEmpty()) {
                builder.addAll(argsArray);
            }
        }
        return builder.toArray(new String[builder.size()]);

    }

    private String findMainClass() {
        if (new MainFunctionDetector(bindingContext).hasMain(currentFile.getDeclarations())) {
            return PackageClassUtils.getPackageClassFqName(currentFile.getPackageFqName()).asString();
        }
        return PackageClassUtils.getPackageClassName(FqName.ROOT);
    }
}
