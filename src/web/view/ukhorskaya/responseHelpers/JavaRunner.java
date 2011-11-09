package web.view.ukhorskaya.responseHelpers;

import org.json.JSONArray;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 12:38 PM
 */


public class JavaRunner {
    //public static final String OUTPUT_DIRECTORY = "C:\\Documents and Settings\\Natalia.Ukhorskaya\\Local Settings\\Temp\\newProject";
    private final List<String> files;
    private final String arguments;
    private final JSONArray jsonArray;

    private boolean isTimeoutException = false;

    public JavaRunner(List<String> files, String arguments, JSONArray array) {
        this.files = files;
        this.arguments = arguments;
        this.jsonArray = array;
    }

    public String getResult() {
        String commandString = generateCommandString();
        Process process;

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
                errStream.append("Timeout exception: impossible to execute your program because it take a lot of time for compilation and execution.");
            }
        }, ServerSettings.TIMEOUT_FOR_EXECUTION);

        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        try {
            String tmp = "";
            while ((!isTimeoutException) && ((tmp = stdError.readLine()) != null)) {
                errStream.append(tmp);
                errStream.append("<br/>");
            }

            while ((!isTimeoutException) && ((tmp = stdInput.readLine()) != null)) {
                outStream.append(tmp);
                outStream.append("<br/>");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return getJsonStringFromErrorMessage("Impossible to run your program: IOException handled until execution");
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.err.println("InterruptedException");
            e.printStackTrace();
            return getJsonStringFromErrorMessage("Impossible to run your program: InterruptedException handled.");
        }

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

        return jsonArray.toString();
    }

    private void deleteFile(String path) {
        File f = new File(ServerSettings.OUTPUT_DIRECTORY + path);
        if (f.exists()) {
            f.delete();
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
        return "[{\"text\":\"" + errorMessage + "<br/>\",\"type\":\"err\"}]";
    }

}
