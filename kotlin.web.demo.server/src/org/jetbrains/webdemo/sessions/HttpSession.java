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

package org.jetbrains.webdemo.sessions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.handlers.ServerResponseUtils;
import org.jetbrains.webdemo.responseHelpers.*;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class HttpSession {
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

            if (parameters.compareType("run")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                sendExecutorResult();
            } else if (parameters.compareType("loadExample")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.LOAD_EXAMPLE);
                ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                sendExampleContent();
            } else if (parameters.compareType("highlight")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
                sessionInfo.setRunConfiguration(parameters.getArgs());
                sendHighlightingResult();
            } else if (parameters.compareType("writeLog")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.WRITE_LOG);
                sendWriteLogResult();
            } else if (parameters.compareType("convertToKotlin")) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                sendConversationResult();
            } else if (parameters.compareType("saveProgram")) {
                sendSaveProgramResult();
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
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Incorrect request"), sessionInfo.getType(), sessionInfo.getOriginUrl(), param);
                writeResponse(ResponseUtils.getErrorInJson("Incorrect request"), HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Throwable e) {
            if (sessionInfo != null && sessionInfo.getType() != null && currentPsiFile != null && currentPsiFile.getText() != null) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", "unknown", "null");
            }
            writeResponse(ResponseUtils.getErrorInJson("Internal server error"), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendConversationResult() {
        PostData data = getPostDataFromRequest();
        writeResponse(new JavaToKotlinConverter(sessionInfo).getResult(data.text), HttpServletResponse.SC_OK);
    }

    private void sendWriteLogResult() {
        String type = parameters.getArgs();
        if (type.equals("info")) {
            String tmp = getPostDataFromRequest(true).text;
            ErrorWriterOnServer.LOG_FOR_INFO.info(tmp);
        } else if (type.equals("errorInKotlin")) {
            String tmp = getPostDataFromRequest(true).text;
            tmp = unescapeXml(unescapeXml(tmp));
            List<String> list = ErrorWriter.parseException(tmp);
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(list.get(2), list.get(3), list.get(1), "unknown" , list.get(4));
        } else {
            String tmp = getPostDataFromRequest(true).text;
            List<String> list = ErrorWriter.parseException(tmp);
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(list.get(2), list.get(3), list.get(1), "unknown", list.get(4));
        }
        writeResponse("Data sent", HttpServletResponse.SC_OK);
    }

    @NonNls
    private static final String[] REPLACES_REFS = {"&lt;", "&gt;", "&amp;", "&#39;", "&quot;"};
    @NonNls private static final String[] REPLACES_DISP = {"<", ">", "&", "'", "\""};

    public static String unescapeXml(@Nullable final String text) {
        if (text == null) return null;
        return StringUtil.replace(text, REPLACES_REFS, REPLACES_DISP);
    }

    private void sendGeneratePublicLinkResult() {
        String result;
        String programId = ResponseUtils.getExampleOrProgramNameByUrl(parameters.getArgs());
        result = MySqlConnector.getInstance().generatePublicLink(programId);
        writeResponse(result, HttpServletResponse.SC_OK);
    }

    private void sendDeleteProgramResult() {
        String result;
        String programId = ResponseUtils.getExampleOrProgramNameByUrl(parameters.getArgs());
        result = MySqlConnector.getInstance().deleteProgram(sessionInfo.getUserInfo(), programId);
        writeResponse(result, HttpServletResponse.SC_OK);
    }

    private void sendLoadProgramResult() {
        String result;
        if (parameters.getArgs().equals("all")) {
            result = MySqlConnector.getInstance().getListOfProgramsForUser(sessionInfo.getUserInfo());
        } else {
            String id;
            if (parameters.getArgs().contains("publicLink")) {
                id = ResponseUtils.getExampleOrProgramNameByUrl(parameters.getArgs());
                result = MySqlConnector.getInstance().getProgramTextByPublicLink(id);
            } else {
                id = ResponseUtils.getExampleOrProgramNameByUrl(parameters.getArgs());
                result = MySqlConnector.getInstance().getProgramText(id);
            }
        }
        writeResponse(result, HttpServletResponse.SC_OK);
    }

    private void sendSaveProgramResult() {
        String result;
        if (parameters.getArgs().startsWith("id=")) {
            String url = ResponseUtils.substringBefore(parameters.getArgs(), "&runConf=");
            String id = ResponseUtils.getExampleOrProgramNameByUrl(url);
            PostData data = getPostDataFromRequest();
            result = MySqlConnector.getInstance().updateProgram(id, data.text, data.arguments, ResponseUtils.substringAfter(parameters.getArgs(), "&runConf="));
        } else {
            PostData data = getPostDataFromRequest();
            String id = ResponseUtils.substringBefore(parameters.getArgs(), "&runConf=");
            result = MySqlConnector.getInstance().saveProgram(sessionInfo.getUserInfo(), id, data.text, data.arguments, ResponseUtils.substringAfter(parameters.getArgs(), "&runConf="));
        }
        writeResponse(result, HttpServletResponse.SC_OK);
    }

    private void sendExecutorResult() {
        PostData data = getPostDataFromRequest();

        sessionInfo.setRunConfiguration(parameters.getArgs());
        if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
            sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
            setGlobalVariables(data.text);

            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(currentPsiFile, data.arguments, sessionInfo);
            writeResponse(responseForCompilation.getResult(), HttpServletResponse.SC_OK);
        } else {
            sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
            writeResponse(new JsConverter(sessionInfo).getResult(data.text, data.arguments), HttpServletResponse.SC_OK);
        }
    }

    private void sendExampleContent() {
        writeResponse(ExamplesList.loadExample(parameters.getArgs()), HttpServletResponse.SC_OK);

    }

    private void setGlobalVariables(@Nullable String text) {
        currentProject = Initializer.INITIALIZER.getEnvironment().getProject();
        if (text == null) {
            text = "fun main(args : Array<String>) {\n" +
                    "  println(\"Hello, world!\")\n" +
                    "}";

        }
        sessionInfo.getTimeManager().saveCurrentTime();
        currentPsiFile = JetPsiFactoryUtil.createFile(currentProject, text);
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(), "PARSER " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: = " + currentPsiFile.getTextLength()));
    }

    private void sendCompletionResult() {
        String positionString = ResponseUtils.substringBefore(parameters.getArgs(), "&runConf=");
        sessionInfo.setRunConfiguration(ResponseUtils.substringAfter(parameters.getArgs(), "&runConf="));
        String[] position = positionString.split(",");
        setGlobalVariables(getPostDataFromRequest().text);

        JsonResponseForCompletion jsonResponseForCompletion = new JsonResponseForCompletion(Integer.parseInt(position[0]), Integer.parseInt(position[1]), currentPsiFile, sessionInfo);
        writeResponse(jsonResponseForCompletion.getResult(), HttpServletResponse.SC_OK);
    }

    private void sendHighlightingResult() {
        setGlobalVariables(getPostDataFromRequest().text);
        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, sessionInfo);
        String response = responseForHighlighting.getResult();
        response = response.replaceAll("\\n", "");
        writeResponse(response, HttpServletResponse.SC_OK);
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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), request.getQueryString());
            writeResponse("Cannot read data from file", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new PostData("", "");
        } finally {
            close(is);
        }

        String finalResponse;
        try {
            finalResponse = TextUtils.decodeUrl(reqResponse.toString());
        } catch (UnsupportedEncodingException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), "null");
            return new PostData("", "");
        } catch (IllegalArgumentException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), reqResponse.toString());
            return new PostData("", "");
        }
        if (finalResponse != null) {
            finalResponse = finalResponse.replaceAll("<br>", "\n");
            if (finalResponse.length() >= 5) {
                if (finalResponse.contains("&consoleArgs=")) {
                    return new PostData(ResponseUtils.substringBetween(finalResponse, "text=", "&consoleArgs="), ResponseUtils.substringAfter(finalResponse, "&consoleArgs="));
                } else {
                    return new PostData(ResponseUtils.substringAfter(finalResponse, "text="));
                }
            } else {
                writeResponse("Post request is too short", HttpServletResponse.SC_BAD_REQUEST);
                return new PostData("", "");
            }
        } else {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Cannot read data from post request"),
                    sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            writeResponse("Cannot read data from post request: ", HttpServletResponse.SC_BAD_REQUEST);
        }

        return new PostData("", "");
    }


    //Send Response
    private void writeResponse(String responseBody, int errorCode) {
        try {
            ServerResponseUtils.writeResponse(request, response, responseBody, errorCode);
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                    sessionInfo.getId(), "ALL " + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + request.getRequestURI() + "?" + request.getQueryString()));
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
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
        ServerResponseUtils.close(closeable);
    }

}
