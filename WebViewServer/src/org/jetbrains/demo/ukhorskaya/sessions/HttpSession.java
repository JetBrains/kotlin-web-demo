package org.jetbrains.demo.ukhorskaya.sessions;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.*;
import org.jetbrains.demo.ukhorskaya.database.MySqlConnector;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesLoader;
import org.jetbrains.demo.ukhorskaya.handlers.ServerHandler;
import org.jetbrains.demo.ukhorskaya.responseHelpers.*;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.jet.lang.psi.JetPsiFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

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

    private HttpServletRequest request;
    private HttpServletResponse response;

    private final SessionInfo sessionInfo;
    private final RequestParameters parameters;

    public HttpSession(SessionInfo info, RequestParameters parameters) {
        this.sessionInfo = info;
        this.parameters = parameters;
    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;
            String param = request.getRequestURI() + "?" + request.getQueryString();

            ErrorWriterOnServer.LOG_FOR_INFO.info("request: " + param + " ip: " + sessionInfo.getId());

            //FOR TEST ONLY
            /*if (parameters.compareType("testConnection")) {
                sendTestConnection();
                return;
            }*/

            if (parameters.compareType("run")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
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
            } else if (parameters.compareType("saveProgram")) {
                sendSaveProgramResult(sessionInfo);
            } else if (parameters.compareType("loadProgram")) {
                sendLoadProgramResult();
            } else if (parameters.compareType("deleteProgram")) {
                sendDeleteProgramResult();
            } else if (parameters.compareType("generatePublicLink")) {
                sendGeneratePublicLinkResult();
            } else if (parameters.compareType("complete")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                sendCompletionResult();
            } else if (parameters.compareType("convertToKotlin")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                sendConvertToKotlinResult();
            } else if (parameters.compareType("loadExample")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.LOAD_EXAMPLE);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                sendExampleContent();
            } else if (parameters.compareType("convertToJs")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                sendConvertToJsResult();
            } else if (parameters.compareType("highlight")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
                sendHighlightingResult();
            } else {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), "Incorrect request", param));
                writeResponse("Incorrect request", HttpStatus.SC_BAD_REQUEST);
            }
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
            writeResponse("Internal server error", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendGeneratePublicLinkResult() {
        String result;
        String programId = ResponseUtils.substringBefore(parameters.getArgs(), "&head=");
        result = MySqlConnector.getInstance().generatePublicLink(programId.replaceAll("%20", " "));
        writeResponse(result, HttpStatus.SC_OK);
    }

    private void sendDeleteProgramResult() {
        String result;
        String programId = ResponseUtils.substringBefore(parameters.getArgs(), "&head=");
        result = MySqlConnector.getInstance().deleteProgram(sessionInfo.getUserInfo(), programId.replaceAll("%20", " "));
        writeResponse(result, HttpStatus.SC_OK);
    }

    private void sendLoadProgramResult() {
        String result;
        if (parameters.getArgs().equals("all")) {
            result = MySqlConnector.getInstance().getListOfProgramsForUser(sessionInfo.getUserInfo());
        } else {
            String id;
            if (parameters.getArgs().contains("publicLink")) {
                id = ResponseUtils.substringBefore(parameters.getArgs(), "&head=");
                result = MySqlConnector.getInstance().getProgramTextByPublicLink(id);
            } else {
                id = ResponseUtils.substringBefore(parameters.getArgs(), "&head=");
                result = MySqlConnector.getInstance().getProgramText(id);
            }
        }
        writeResponse(result, HttpStatus.SC_OK);
    }

    private void sendSaveProgramResult(SessionInfo sessionInfo) {
        String result;
        if (parameters.getArgs().startsWith("id=")) {
            String id = ResponseUtils.substringBetween(parameters.getArgs(), "id=", "&head=");
            PostData data = getPostDataFromRequest();
            result = MySqlConnector.getInstance().updateProgram(id.replaceAll("%20", " "), data.text, data.arguments);
        } else {
            PostData data = getPostDataFromRequest();
            result = MySqlConnector.getInstance().saveProgram(sessionInfo.getUserInfo(), parameters.getArgs().replaceAll("%20", " "), data.text, data.arguments);
        }
        writeResponse(result, HttpStatus.SC_OK);
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
        writeResponse(loader.getResultByNameAndHead(parameters.getArgs()), HttpStatus.SC_OK);

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
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(e);
            }

            writeResponse("Response sended", HttpStatus.SC_OK);
        } else {
            StringBuilder responseStr = new StringBuilder();
            PrintWriter writer = null;
            String path = "/testConnection.html";
            InputStream is = ServerHandler.class.getResourceAsStream(path);
            try {
                responseStr.append(ResponseUtils.readData(is));
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error("Cannot read data from file", e);
                writeResponse("Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return;
            } finally {
                close(is);
            }

            try {
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                writer = response.getWriter();
                writer.write(responseStr.toString());
            } catch (IOException e) {
                //This is an exception we can't send data to client
            } finally {
                close(writer);
            }
        }
    }

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
            is = request.getInputStream();
            reqResponse.append(ResponseUtils.readData(is, withNewLines));
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, "getPostDataFromRequest " + request.getQueryString()));
            writeResponse("Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return new PostData("", "");
        } finally {
            close(is);
        }

        String finalResponse;
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
    private void writeResponse(String responseBody, int errorCode) {
        PrintWriter writer = null;

        try {

            response.setStatus(errorCode);
            writer = response.getWriter();
            writer.write(responseBody);
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                    sessionInfo.getId(), "ALL " + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + request.getRequestURI() + "?" + request.getQueryString()));
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
        } finally {
            close(writer);
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
