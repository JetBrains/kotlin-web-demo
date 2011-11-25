package web.view.ukhorskaya.responseHelpers;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.server.ServerSettings;
import web.view.ukhorskaya.sessions.HttpSession;

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
    private final String arguments;
    private final JSONArray jsonArray;
    private final String textFromfile;

    private volatile boolean isTimeoutException = false;

    public JavaRunner(List<String> files, String arguments, JSONArray array, String text) {
        this.files = files;
        this.arguments = arguments;
        this.jsonArray = array;
        this.textFromfile = text;
    }

    public String getResult() {
        String commandString = generateCommandString();
        Process process;
        HttpSession.TIME_MANAGER.saveCurrentTime();
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
                ErrorsWriter.LOG_FOR_INFO.info(ErrorsWriter.getInfoForLog(HttpSession.TYPE.name(), HttpSession.SESSION_ID, "Timeout exception."));
                errStream.append("Program was terminated after " + Integer.parseInt(ServerSettings.TIMEOUT_FOR_EXECUTION) / 1000 + "s.");
            }
        }, Integer.parseInt(ServerSettings.TIMEOUT_FOR_EXECUTION));

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
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(HttpSession.TYPE.name(), e, textFromfile));
            return ResponseUtils.getErrorInJson("Impossible to run your program: InterruptedException handled.");
        }
        ErrorsWriter.LOG_FOR_INFO.info(ErrorsWriter.getInfoForLog(HttpSession.TYPE.name(), HttpSession.SESSION_ID, "RUN user program " + HttpSession.TIME_MANAGER.getMillisecondsFromSavedTime()
                + " timeout=" + isTimeoutException
                + " commandString=" + commandString));

        if ((exitValue == 1) && !isTimeoutException) {
            if (outStream.length() > 0) {
                if (outStream.indexOf("An error report file with more information is saved as:") != -1) {
                    outStream.delete(0, outStream.length());
                    errStream.append(ServerSettings.KOTLIN_ERROR_MESSAGE);
                    String linkForLog = getLinkForLog(outStream.toString());
                    ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(HttpSession.TYPE.name(), "Error from log", linkForLog));
                }
                ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(HttpSession.TYPE.name(), outStream.toString().replaceAll("<br/>", "\n"), textFromfile));
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
                map.put("text", ServerSettings.KOTLIN_ERROR_MESSAGE);
                jsonArray.put(map);
                mapErr.put("type", "out");
            } else {
                ErrorsWriter.LOG_FOR_INFO.error(ErrorsWriter.getInfoForLog(HttpSession.TYPE.name(), HttpSession.SESSION_ID, "error while excecution: " + errStream));
                mapErr.put("type", "err");
            }
            mapErr.put("text", errStream.toString());
            jsonArray.put(mapErr);
        }


        for (String fileName : files) {
            deleteFile(fileName);
        }

        timer.cancel();
        return jsonArray.toString();
    }

    private String getLinkForLog(String outStream) {
        String path = ResponseUtils.substringAfter(outStream, "An error report file with more information is saved as:" + ResponseUtils.addNewLine() + "# ");
        path = ResponseUtils.substringBefore(path, ResponseUtils.addNewLine() + "#");
        File log = new File(path);
        /*if (log.exists()) {
            String links = ResponseUtils.generateHtmlTag("a", "view", "href", "/log=" + log.getAbsolutePath() + "&view");
            links += ResponseUtils.generateHtmlTag("a", "download", "href", "/log=" + log.getAbsolutePath() + "&download");
            return links;
        }*/
        try {
            String response = ResponseUtils.readData(new FileReader(log), true);
            log.deleteOnExit();
            return response;
        } catch (IOException e) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(HttpSession.TYPE.name(), e, "Impossible to find " + log.getAbsolutePath()));
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
        ErrorsWriter.LOG_FOR_EXCEPTIONS.error(
                ErrorsWriter.getExceptionForLog(HttpSession.TYPE.name(),
                        message, stackTrace, textFromfile));
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
//                    ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.GET_RESOURCE.name(), e, exchange.getRequestURI().toString()));
//                    writeResponse(exchange, "Could not load the resource from the server".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        return returnValue;
    }

    private void deleteFile(String path) {
        File f = new File(ServerSettings.OUTPUT_DIRECTORY + File.separatorChar + path);
        File parent = f.getParentFile();
        while (!parent.getAbsolutePath().equals(ServerSettings.OUTPUT_DIRECTORY)) {
            f = parent;
            parent = parent.getParentFile();
        }
        delete(f);
    }

    private void delete(File file) {
        if (file.isDirectory() && file.exists()) {
            if (file.list().length == 0) {
                if (file.exists()) {
                    file.delete();
                    ErrorsWriter.LOG_FOR_INFO.info(ErrorsWriter.getInfoForLog(HttpSession.TYPE.name(), HttpSession.SESSION_ID, "Directory is deleted : " + file.getAbsolutePath()));
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
                        ErrorsWriter.LOG_FOR_INFO.info(ErrorsWriter.getInfoForLog(HttpSession.TYPE.name(), HttpSession.SESSION_ID, "Directory is deleted : " + file.getAbsolutePath()));
                    }
                }
            }
        } else {
            if (file.exists()) {
                file.delete();
                ErrorsWriter.LOG_FOR_INFO.info(ErrorsWriter.getInfoForLog(HttpSession.TYPE.name(), HttpSession.SESSION_ID, "File is deleted : " + file.getAbsolutePath()));
            }
        }
    }

    private String generateCommandString() {
        StringBuilder builder = new StringBuilder("java ");
        builder.append("-classpath ");
        //builder.append(System.getProperty("java.class.path"));
        builder.append(ServerSettings.OUTPUT_DIRECTORY);
        builder.append(File.pathSeparator);
        builder.append("WebView.jar");
        builder.append(File.pathSeparator);
        builder.append(ServerSettings.PATH_TO_KOTLIN_LIB);
        builder.append(" ");
        builder.append("-Djava.security.manager ");
        builder.append(modifyClassNameFromPath(files.get(0)));
        builder.append(" ");
        builder.append(arguments);
        return builder.toString();
    }

    private String modifyClassNameFromPath(String path) {
        String name = ResponseUtils.substringBefore(path, ".class");
        name = name.replaceAll("/", ".");
        return name;
    }
}
