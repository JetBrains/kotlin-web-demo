package org.jetbrains.demo.ukhorskaya.sessions;

import com.google.common.io.Files;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.*;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesLoader;
import org.jetbrains.demo.ukhorskaya.responseHelpers.*;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.jet.lang.psi.JetPsiFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 9/6/11
 * Time: 12:14 PM
 */

public class HttpSession {
    //    private static Logger LOG;
    protected Project currentProject;
    protected PsiFile currentPsiFile;

    private HttpExchange exchange;

    private final SessionInfo sessionInfo;
    private final RequestParameters parameters;

    public HttpSession(SessionInfo info, RequestParameters parameters) {
        this.sessionInfo = info;
        this.parameters = parameters;
    }

    public void handle(final HttpExchange exchange) {
        try {
            this.exchange = exchange;
            //String param = exchange.getRequestURI().toString();

            ErrorWriterOnServer.LOG_FOR_INFO.info("request: " + exchange.getRequestURI().toString() + " ip: " + sessionInfo.getIp());

            //FOR TEST ONLY
            if (parameters.compareType("testConnection")) {
                sendTestConnection();
                return;
            }

            if (parameters.compareType("run")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getIp(), sessionInfo.getType()));
                sendExecutorResult();
            } else if (parameters.compareType("writeLog")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.WRITE_LOG);
                String type = parameters.getArgs();
                if (type.equals("info")) {
                    String tmp = getPostDataFromRequest(true).text;
                    ErrorWriterOnServer.LOG_FOR_INFO.info(tmp);
                } else {
                    String tmp = getPostDataFromRequest(true).text;
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(tmp);
                }
                writeResponse("Data sent", HttpStatus.SC_OK);
            } else if (parameters.compareType("complete")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getIp(), sessionInfo.getType()));
                sendCompletionResult();
            } else if (parameters.compareType("convertToKotlin")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getIp(), sessionInfo.getType()));
                sendConvertToKotlinResult();
            } else if (parameters.compareType("loadExample")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.LOAD_EXAMPLE);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getIp(), sessionInfo.getType()));
                sendExampleContent();
            } else if (parameters.compareType("convertToJs")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getIp(), sessionInfo.getType()));
                sendConvertToJsResult();
            }else if (parameters.compareType("highlight")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
                if (!sessionInfo.getIp().equals("127.0.0.1")) {
                    ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getIp(), sessionInfo.getType()));
                }
                sendHighlightingResult();
            } else {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), "Incorrect request", exchange.getRequestURI().toString()));
                writeResponse("Incorrect request", HttpStatus.SC_BAD_REQUEST);
            }
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
            writeResponse("Internal server error", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendConvertToJsResult() {
        PostData data = getPostDataFromRequest(true);
        writeResponse(new JsConverter(sessionInfo).getResult(data.text, data.arguments), HttpStatus.SC_OK);
    }

    private void sendConvertToKotlinResult() {
        PostData data = getPostDataFromRequest(true);
        writeResponse(new JavaConverterRunner(data.text, data.arguments, sessionInfo).getResult(), HttpStatus.SC_OK);
    }

    private void sendExampleContent() {
        ExamplesLoader loader = new ExamplesLoader();
        /*String idStr = ResponseUtils.substringBefore(parameters.getArgs(), "&head=");
        String headName = ResponseUtils.substringAfter(parameters.getArgs(), "&head=");*/
        writeResponse(loader.getResultByExampleName(parameters.getArgs()), HttpStatus.SC_OK);

    }

    //FOR TEST ONLY
    private void sendTestConnection() {
        if (parameters.getArgs().contains("stopTest=true")) {
            try {
                String response = getPostDataFromRequest().text;
                File file = new File(ServerSettings.TEST_CONNECTION_OUTPUT + File.separator + "testConnection" + System.nanoTime() + ".csv");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(response);
                writer.close();
            } catch (IOException e) {
//                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(e, e);
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(e);
            }

            writeResponse("Response sended", HttpStatus.SC_OK);
        } else {
            StringBuilder response = new StringBuilder();
            InputStream is = null;
            try {
                is = HttpSession.class.getResourceAsStream("/testConnection.html");
                response.append(ResponseUtils.readData(is, true));
            } catch (FileNotFoundException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "logs.html not found"));
                writeResponse("Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
                return;
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "Exception until downloading logs.html"));
                writeResponse("Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
                return;
            }

            OutputStream os = null;
            try {
                exchange.sendResponseHeaders(HttpStatus.SC_OK, response.toString().length());
                os = exchange.getResponseBody();
                os.write(response.toString().getBytes());
            } catch (IOException e) {
                //This is an exception we can't send data to client
            } finally {
                close(os);
                exchange.close();
            }
        }
    }
    /*if (exchange.getRequestURI().toString().contains("stopTest=true")) {
            try {
                String response = getPostDataFromRequest().text;
                File file = new File(ServerSettings.TEST_CONNECTION_OUTPUT + File.separator + "testConnection" + System.nanoTime() + ".csv");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(response);
                writer.close();
            } catch (IOException e) {
//                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(e, e);
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(e);
            }

            writeResponse("Response sended", HttpStatus.SC_OK);
        } else {
            try {
                exchange.sendResponseHeaders(400, 0);
                ByteStreams.copy(new FileInputStream(ServerSettings.RESOURCES_ROOT + File.separator + "testConnection.html"), exchange.getResponseBody());
            } catch (FileNotFoundException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "logs.html not found"));
                writeResponse("Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "Exception until downloading logs.html"));
                writeResponse("Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
            } finally {
                exchange.close();
            }
        }*/

    private void setGlobalVariables(@Nullable String text) {
        currentProject = Initializer.getEnvironment().getProject();
        if (text == null) {
            text = "fun main(args : Array<String>) {\n" +
                    "  System.out?.println(\"Hello, world!\")\n" +
                    "}";

        }
        sessionInfo.getTimeManager().saveCurrentTime();
        currentPsiFile = JetPsiFactory.createFile(currentProject, text);
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(), "PARSER " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: = " + currentPsiFile.getTextLength()));
    }


    private void sendCompletionResult() {

        String param = exchange.getRequestURI().getQuery();
        String[] position = parameters.getArgs().split(",");

        setGlobalVariables(getPostDataFromRequest().text);

        JsonResponseForCompletion jsonResponseForCompletion = new JsonResponseForCompletion(Integer.parseInt(position[0]), Integer.parseInt(position[1]), currentPsiFile, sessionInfo);
        writeResponse(jsonResponseForCompletion.getResult(), HttpStatus.SC_OK);
    }

    private void sendHighlightingResult() {
            setGlobalVariables(getPostDataFromRequest().text);
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, sessionInfo);

            String response = responseForHighlighting.getResult();
            response = response.replaceAll("\\n", "");
            writeResponse(response, HttpStatus.SC_OK);
    }


    private PostData getPostDataFromRequest() {
        return getPostDataFromRequest(false);
    }

    private PostData getPostDataFromRequest(boolean withNewLines) {
        StringBuilder reqResponse = new StringBuilder();
        InputStream is = null;
        try {
            is = exchange.getRequestBody();
            reqResponse.append(ResponseUtils.readData(is, withNewLines));
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, "getPostDataFromRequest " + exchange.getRequestURI()));
            writeResponse("Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return new PostData("", "");
        } finally {
            close(is);
        }

        String finalResponse = null;
        try {
            finalResponse = URLDecoder.decode(reqResponse.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, "null"));
            return new PostData("", "");
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
                return new PostData("", "");
            }
        } else {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), "Cannot read data from post request.", currentPsiFile.getText()));
            writeResponse("Cannot read data from post request: ", HttpStatus.SC_BAD_REQUEST);
        }

        return new PostData("", "");
    }

    private void sendExecutorResult() {
        PostData data = getPostDataFromRequest();
        setGlobalVariables(data.text);

        CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(currentPsiFile, data.arguments, sessionInfo);
        writeResponse(responseForCompilation.getResult(), HttpStatus.SC_OK);
    }

    //Send Response
    //disableHeaders - disable htmlPatern header for answer
    private void writeResponse(String responseBody, int errorCode) {
        OutputStream os = null;

        try {

            byte[] bytes = responseBody.getBytes();
            exchange.sendResponseHeaders(errorCode, bytes.length);
            os = exchange.getResponseBody();
            os.write(bytes);
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                    sessionInfo.getId(), "ALL " + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + exchange.getRequestURI()));
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
            }
            exchange.close();
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

    private void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("UNKNOWN", e, " NULL"));
        }
    }

}
