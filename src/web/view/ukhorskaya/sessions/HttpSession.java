package web.view.ukhorskaya.sessions;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import web.view.ukhorskaya.Initializer;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.TimeManager;
import web.view.ukhorskaya.handlers.ServerHandler;
import web.view.ukhorskaya.responseHelpers.CompileAndRunExecutor;
import web.view.ukhorskaya.responseHelpers.JsonResponseForCompletion;
import web.view.ukhorskaya.responseHelpers.JsonResponseForHighlighting;

import java.io.*;
import java.net.URLDecoder;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 9/6/11
 * Time: 12:14 PM
 */

public class HttpSession {
    private static final Logger LOG = Logger.getLogger(HttpSession.class);
    public static TimeManager TIME_MANAGER;
    public static int SESSION_ID;

    protected Project currentProject;
    protected PsiFile currentPsiFile;

    private HttpExchange exchange;

    public HttpSession() {
        TIME_MANAGER = new TimeManager();
    }

    public void handle(final HttpExchange exchange) {
        this.exchange = exchange;
        String param = exchange.getRequestURI().toString();

        //FOR TEST ONLY
        if (param.contains("testConnection")) {
            sendTestConnection();
            return;
        }

        String sId = ResponseUtils.substringBetween(param, "?sessionId=", "&");
        if (sId.equals("") || sId.equals("undefined")) {
            SESSION_ID = RandomUtils.nextInt();
            ServerHandler.numberOfUsers++;
            LOG.info("Number of users since start server: " + ServerHandler.numberOfUsers);
        } else {
            SESSION_ID = Integer.parseInt(sId);
        }

        if (param.contains("compile=true") || param.contains("run=true")) {
            sendExecutorResult();
        } else if (param.contains("complete=true")) {
            sendCompletionResult();
        } else {
            sendProjectSourceFile();
        }
    }

    //FOR TEST ONLY
    private void sendTestConnection() {
        if (exchange.getRequestURI().toString().contains("stopTest=true")) {
            try {
                String response = getPostDataFromRequest().text;
                File file = new File("C:/Development/testData/testConnection" + System.nanoTime() + ".csv");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(response);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            writeResponse("Response sended", HttpStatus.SC_OK, true);
        } else {
            StringBuilder response = new StringBuilder();
            OutputStream os = null;
            String path = "/testConnection.html";
            InputStream is = ServerHandler.class.getResourceAsStream(path);
            try {
                response.append(ResponseUtils.readData(is));
            } catch (IOException e) {
                LOG.error("Cannot read data from file", e);
                writeResponse("Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR, true);
                return;
            }

            try {
                exchange.sendResponseHeaders(400, response.toString().length());
                os = exchange.getResponseBody();
                os.write(response.toString().getBytes());
            } catch (IOException e) {
                //This is an exception we can't send data to client
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
    }

    private void setGlobalVariables(@Nullable String text) {
        currentProject = Initializer.getEnvironment().getProject();
        if (text == null) {
            text = "namespace demo\n" +
                    "\n" +
                    "class Main() {\n" +
                    "\n" +
                    "  fun aaa() : Int {return 10}\n" +
                    "}\n" +
                    "\n" +
                    "fun main(args : Array<String>) {\n" +
                    "  var s = \"Natalia\";\n" +
                    "  //Thread.sleep(16000)\n" +
                    "    System.out?.println(\"Hello, \" + s) \n" +
                    "    System.out?.println(\"ERROR\"); \n" +
                    "  //Thread.sleep(16000)\n" +
                    "  //java.io.FileWriter(\"sdfs.kt\")\n" +
                    "} ";
        }
        TIME_MANAGER.saveCurrentTime();
        currentPsiFile = JetPsiFactory.createFile(currentProject, text);
        LOG.info("userId=" + SESSION_ID + " PARSER " + TIME_MANAGER.getMillisecondsFromSavedTime());
    }


    private void sendCompletionResult() {

        String param = exchange.getRequestURI().getQuery();
        String[] position = ResponseUtils.substringBetween(param, "cursorAt=", "&time=").split(",");

        setGlobalVariables(getPostDataFromRequest().text);

        JsonResponseForCompletion jsonResponseForCompletion = new JsonResponseForCompletion(Integer.parseInt(position[0]), Integer.parseInt(position[1]), currentPsiFile);
        writeResponse(jsonResponseForCompletion.getResult(), HttpStatus.SC_OK, true);
    }

    private void sendProjectSourceFile() {
        String param = exchange.getRequestURI().getQuery();

        if ((param != null) && (param.contains("sendData=true"))) {
            setGlobalVariables(getPostDataFromRequest().text);
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile);

            String response = responseForHighlighting.getResult();
            response = response.replaceAll("\\n", "");
            writeResponse(response, HttpStatus.SC_OK, true);
        } else {
            setGlobalVariables(null);
            writeResponse(currentPsiFile.getText(), HttpStatus.SC_OK);
        }
    }

    private PostData getPostDataFromRequest() {
        StringBuilder reqResponse = new StringBuilder();
        try {
            reqResponse.append(ResponseUtils.readData(exchange.getRequestBody()));
        } catch (IOException e) {
            LOG.error("Cannot read data from file", e);
            writeResponse("Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR, true);
            return new PostData("", "");
        }

        String finalResponse = null;
        try {
            finalResponse = URLDecoder.decode(reqResponse.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Impossible to write to file in UTF-8");
        }
        if (finalResponse != null) {
            finalResponse = finalResponse.replaceAll("<br>", "\n");
            if (finalResponse.length() > 5) {
                if (finalResponse.contains("&consoleArgs=")) {
                    return new PostData(ResponseUtils.substringBetween(finalResponse, "text=", "&consoleArgs="), ResponseUtils.substringAfter(finalResponse, "&consoleArgs="));
                } else {
                    return new PostData(ResponseUtils.substringAfter(finalResponse, "text="));
                }
            } else {
                writeResponse("Post request is too short", HttpStatus.SC_BAD_REQUEST);
            }
        } else {
            LOG.error("Cannot read data from post request: " + exchange.getRequestURI());
            writeResponse("Cannot read data from post request: ", HttpStatus.SC_BAD_REQUEST, true);
        }

        return null;
    }

    private void sendExecutorResult() {
        PostData data = getPostDataFromRequest();
        setGlobalVariables(data.text);
        boolean isOnlyCompilation = true;
        if (exchange.getRequestURI().getQuery().contains("compile=true")) {
            isOnlyCompilation = true;
        } else if (exchange.getRequestURI().getQuery().contains("run=true")) {
            isOnlyCompilation = false;
        }

        CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(isOnlyCompilation, currentPsiFile, data.arguments);
        writeResponse(responseForCompilation.getResult(), HttpStatus.SC_OK, true);
    }

    private void writeResponse(String responseBody, int errorCode) {
        writeResponse(responseBody, errorCode, false);
    }

    //Send Response
    //disableHeaders - disable html header for answer
    private void writeResponse(String responseBody, int errorCode, boolean disableHeaders) {
        OutputStream os = null;
        StringBuilder response = new StringBuilder();

        String path;
        if (!disableHeaders) {
            path = "/header.html";
            InputStream is = ServerHandler.class.getResourceAsStream(path);
            if (is == null) {
                LOG.error("Cannot find header.html for request: " + exchange.getRequestURI());
                writeResponse("File not found", HttpStatus.SC_NOT_FOUND);
                return;
            }
            try {
                response.append(ResponseUtils.readData(is));
            } catch (IOException e) {
                LOG.error("Cannot read data from file", e);
                writeResponse("Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR, true);
                return;
            }
        } else {
            response.append("$RESPONSEBODY$");
        }

        String finalResponse = response.toString();
        finalResponse = finalResponse.replace("$RESPONSEBODY$", responseBody);
        if (!disableHeaders) {
            finalResponse = finalResponse.replace("$SESSIONID$", String.valueOf(SESSION_ID));
        }
        try {
            //ONLY FOR TEST
            if (exchange.getRequestURI().toString().contains("&time=")) {
                String query = exchange.getRequestURI().getQuery();
                query = query.substring(query.indexOf("&time=") + 6);
                exchange.getResponseHeaders().add("time", query);
            }

            exchange.sendResponseHeaders(errorCode, finalResponse.length());
            os = exchange.getResponseBody();
            os.write(finalResponse.getBytes());
            LOG.info("userId=" + SESSION_ID + " ALL SESSION: " + TIME_MANAGER.getMillisecondsFromStart() + " request=" + exchange.getRequestURI());
        } catch (IOException e) {
            //This is an exception we can't send data to client
            LOG.error("Error while writing response.", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                LOG.error("Error while closing outputStream.", e);
            }
        }
    }

    private class PostData {
        public final String text;
        public String arguments = null;

        private PostData(String text) {
            this.text = text;
        }

        private PostData(String text, String arguments) {
            this.text = text;
            this.arguments = arguments;
        }
    }


}
