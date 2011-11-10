package web.view.ukhorskaya.responseHelpers;

import org.apache.log4j.Logger;
import org.json.JSONArray;
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

    private volatile boolean isTimeoutException = false;

    public JavaRunner(List<String> files, String arguments, JSONArray array) {
        this.files = files;
        this.arguments = arguments;
        this.jsonArray = array;
    }

    public String getResult() {
        String commandString = generateCommandString();
        Process process;
        HttpSession.TIME_MANAGER.saveCurrentTime();
        try {
            process = Runtime.getRuntime().exec(commandString);
            process.getOutputStream().close();
        } catch (IOException e) {
            return getJsonStringFromErrorMessage("Impossible to run your program: IOException handled until execution");
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
        }, ServerSettings.TIMEOUT_FOR_EXECUTION);

        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        try {
            readStream(errStream, stdError);
            readStream(outStream, stdInput);

        } catch (IOException e) {
            e.printStackTrace();
            return getJsonStringFromErrorMessage("Impossible to run your program: IOException handled until execution");
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            LOG.error("Interrupted exception during java process excution", e);
            return getJsonStringFromErrorMessage("Impossible to run your program: InterruptedException handled.");
        }
        LOG.info("userId=" + HttpSession.SESSION_ID + " RUN user program " + HttpSession.TIME_MANAGER.getMillisecondsFromSavedTime() + " timeout=" + isTimeoutException);
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

        for (String fileName : files) {
            deleteFile(fileName);
        }

        timer.cancel();
        return jsonArray.toString();
    }

    private void readStream(StringBuilder errStream, BufferedReader stdError) throws IOException {
        String tmp;
        while ((!isTimeoutException) && ((tmp = stdError.readLine()) != null)) {
            errStream.append(tmp);
            errStream.append(ResponseUtils.addNewLine());
        }
    }

    private void deleteFile(String path) {
        File f = new File(ServerSettings.OUTPUT_DIRECTORY + path);
        if (f.exists()) {
            boolean del = f.delete();
            if (!del) {
                LOG.error("userId=" + HttpSession.SESSION_ID + " File " + ServerSettings.OUTPUT_DIRECTORY + path + " isn't deleted.");
            }
        }
    }

    private String generateCommandString() {
        StringBuilder builder = new StringBuilder("java ");
        builder.append("-classpath ");
        //builder.append(System.getProperty("java.class.path"));
        builder.append(ServerSettings.OUTPUT_DIRECTORY);
        builder.append(";");
        builder.append(ServerSettings.PATH_TO_KOTLIN_LIB);
        builder.append(" ");
        builder.append("-Djava.security.manager ");
        builder.append(modifyClassNameFromPath(files.get(0)));
        builder.append(" ");
        builder.append(arguments);
        return builder.toString();
    }

    private String modifyClassNameFromPath(String path) {
        String name = "";
        name = ResponseUtils.substringAfter(path, "out\\");
        if (name.equals("")) {
            name = path;
        }
        name = ResponseUtils.substringBefore(name, ".class");
        name = name.replaceAll("/", ".");
        return name;
    }

    private String getJsonStringFromErrorMessage(String errorMessage) {
        return "[{\"text\":\"" + errorMessage + ResponseUtils.addNewLine() + "\",\"type\":\"err\"}]";
    }

}
