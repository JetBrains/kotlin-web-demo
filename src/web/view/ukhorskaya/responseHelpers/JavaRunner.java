package web.view.ukhorskaya.responseHelpers;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.server.ServerSettings;
import web.view.ukhorskaya.sessions.HttpSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 12:38 PM
 */

public class JavaRunner {
    private final Logger LOG = Logger.getLogger(JavaRunner.class);

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
                LOG.info("userId=" + HttpSession.SESSION_ID + " Timeout exception.");
                errStream.append("Timeout exception: impossible to execute your program because it take a lot of time for compilation and execution.");
            }
        }, Integer.parseInt(ServerSettings.TIMEOUT_FOR_EXECUTION));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        try {
            readStream(errStream, stdError);
            readStream(outStream, stdInput);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseUtils.getErrorInJson("Impossible to run your program: IOException handled until execution");
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            LOG.error("Interrupted exception during java process execution", e);
            return ResponseUtils.getErrorInJson("Impossible to run your program: InterruptedException handled.");
        }
        LOG.info("userId= " + HttpSession.SESSION_ID + " RUN user program " + HttpSession.TIME_MANAGER.getMillisecondsFromSavedTime()
                + " timeout=" + isTimeoutException
                + " commandString=" + commandString);
        if (!isTimeoutException) {
            Map<String, String> mapOut = new HashMap<String, String>();
            mapOut.put("type", "out");
            mapOut.put("text", outStream.toString());
            jsonArray.put(mapOut);
        }

        Map<String, String> mapErr = new HashMap<String, String>();
        mapErr.put("type", "err");
        mapErr.put("text", errStream.toString());
        jsonArray.put(mapErr);

        if (errStream.length() > 0) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(
                    ErrorsWriter.getExceptionForLog(HttpSession.TYPE.name(), "Error while execution of user program: " + errStream,
                            textFromfile));
        }

        for (String fileName : files) {
            deleteFile(fileName);
        }

        timer.cancel();
        return jsonArray.toString();
    }

    private void readStream(StringBuilder errStream, BufferedReader stdError) throws IOException {
        String tmp;
        while ((!isTimeoutException) && ((tmp = stdError.readLine()) != null)) {
            errStream.append(ResponseUtils.escapeString(tmp));
            errStream.append(ResponseUtils.addNewLine());
        }
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
                    LOG.info("userId=" + HttpSession.SESSION_ID + " Directory is deleted : " + file.getAbsolutePath());
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
                        LOG.info("userId=" + HttpSession.SESSION_ID + " Directory is deleted : " + file.getAbsolutePath());
                    }
                }
            }
        } else {
            if (file.exists()) {
                file.delete();
                LOG.info("userId=" + HttpSession.SESSION_ID + " File is deleted : " + file.getAbsolutePath());
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
