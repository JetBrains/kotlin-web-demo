package org.jetbrains.demo.ukhorskaya.handlers;

import com.google.common.io.ByteStreams;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.*;
import org.jetbrains.demo.ukhorskaya.authorization.*;
import org.jetbrains.demo.ukhorskaya.database.MySqlConnector;
import org.jetbrains.demo.ukhorskaya.session.UserInfo;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesList;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesLoader;
import org.jetbrains.demo.ukhorskaya.help.HelpLoader;
import org.jetbrains.demo.ukhorskaya.log.LogDownloader;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.demo.ukhorskaya.sessions.HttpSession;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/10/11
 * Time: 1:16 PM
 */

public class ServerHandler {

    public ServerHandler() {

    }

    public static String HOST;

    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        HOST = InetAddress.getLocalHost().getHostName();

        if (request.getQueryString() != null && request.getQueryString().equals("test")) {
            PrintWriter out = null;
            try {
                out = response.getWriter();
                out.write("ok");
            } catch (Throwable e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("test", e, "null"));
            } finally {
                close(out);
            }
            return;
        }

        SessionInfo sessionInfo;

        String param = request.getRequestURI() + "?" + request.getQueryString();
        try {
            RequestParameters parameters = RequestParameters.parseRequest(param);
            if (parameters.compareType("sendUserData")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                MySqlConnector.getInstance().findUser(sessionInfo.getUserInfo());
                sendUserInformation(request, response, sessionInfo);
            } else if (parameters.compareType("getSessionId")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                String id = sessionInfo.getId();
                PrintWriter out = null;
                try {
                    out = response.getWriter();
                    JSONArray array = new JSONArray();
                    array.put(id);
                    if (sessionInfo.getUserInfo().isLogin()) {
                        array.put(URLEncoder.encode(sessionInfo.getUserInfo().getName(), "UTF-8"));
                    }
                    out.write(array.toString());
                } catch (Throwable e) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(param, e, "null"));
                } finally {
                    close(out);
                }
            } else if (parameters.compareType("authorization")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                sendAuthorizationResult(request, response, parameters, sessionInfo);
            } else if (parameters.compareType("updateExamples")) {
                updateExamples(response);
            } else if (param.startsWith("/logs") || parameters.compareType("updateStatistics")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                if (parameters.compareType("updateStatistics")) {
                    sendListLogs(request, response, true);
                } else {
                    sendListLogs(request, response, false);
                }
            } else if (parameters.compareType("showUserInfo")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendUserInfoForStatistics(response);
            } else if (parameters.compareType("sortExceptions")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name());
                sendSortedExceptions(request, response, parameters);
            } else if (parameters.compareType("downloadLog")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name() + " " + param);
                sendLog(request, response, parameters);
            } else if (parameters.compareType("loadExample") && parameters.getArgs().equals("all")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                sendExamplesList(response);
            } else if (parameters.compareType("loadHelpForExamples")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_EXAMPLES.name());
                sendHelpContentForExamples(response);
            } else if (parameters.compareType("loadHelpForWords")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_WORDS.name());
                sendHelpContentForWords(response);
            } else if (parameters.compareType("highlight")
                    || parameters.compareType("complete")
                    || parameters.compareType("run")
                    || parameters.compareType("convertToJs")
                    || parameters.compareType("loadExample")
                    || parameters.compareType("saveProgram")
                    || parameters.compareType("deleteProgram")
                    || parameters.compareType("generatePublicLink")
                    || parameters.compareType("loadProgram")
                    || parameters.compareType("writeLog")) {
                if (!parameters.compareType("writeLog")) {
                    sessionInfo = setSessionInfo(request, parameters.getSessionId());
                } else {
                    sessionInfo = new SessionInfo(request.getSession().getId());
                }
                if (!parameters.getSessionId().equals(sessionInfo.getId())) {
                    parameters.setSessionId(sessionInfo.getId());
                }
                HttpSession session = new HttpSession(sessionInfo, parameters);
                session.handle(request, response);
            } else {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_RESOURCE.name() + " " + param);
                sendResourceFile(request, response);
            }
        } catch (Throwable e) {
            //Do not stop server
            e.printStackTrace();
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(param, e, "null"));
            writeResponse(response, "Internal server error", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendAuthorizationResult(HttpServletRequest request, HttpServletResponse response, RequestParameters parameters, SessionInfo sessionInfo) {
        if (parameters.getArgs().equals("logout")) {
            sessionInfo.getUserInfo().logout();
            request.getSession().setAttribute("userInfo", sessionInfo.getUserInfo());
        } else {
            AuthorizationHelper helper;
            if (parameters.getArgs().contains("twitter")) {
                helper = new AuthorizationTwitterHelper();
            } else if (parameters.getArgs().contains("google")) {
                helper = new AuthorizationGoogleHelper();
            } else {
                helper = new AuthorizationFacebookHelper();
            }
            if (parameters.getArgs().contains("oauth_verifier") || parameters.getArgs().contains("code=")) {
                UserInfo info = helper.verify(parameters.getArgs());
                if (info != null) {

                    sessionInfo.setUserInfo(info);
                    MySqlConnector.getInstance().addNewUser(sessionInfo.getUserInfo());
                    request.getSession().setAttribute("userInfo", sessionInfo.getUserInfo());
                }
                try {
                    response.sendRedirect("http://" + HOST);
                } catch (IOException e) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot redirect", e, "null"));
                }
            } else if (parameters.getArgs().contains("denied=")) {
                try {
                    response.sendRedirect("http://" + HOST);
                } catch (IOException e) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot redirect", e, "null"));
                }
            } else {
                String verifyKey = helper.authorize();
                PrintWriter out = null;
                try {
                    out = response.getWriter();
                    out.write(verifyKey);
                } catch (Throwable e) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot verify", e, "null"));
                } finally {
                    close(out);
                }
            }
        }
    }

    private void sendUserInfoForStatistics(final HttpServletResponse response) {
        writeResponse(response, Statistics.getInstance().showMap(), HttpStatus.SC_OK);
    }


    private void updateExamples(final HttpServletResponse response) {
        String responseStr = ExamplesList.updateList();
        responseStr += HelpLoader.updateExamplesHelp();
        writeResponse(response, responseStr, HttpStatus.SC_OK);
    }

    private void sendSortedExceptions(final HttpServletRequest request, final HttpServletResponse response, RequestParameters parameters) {
        if (parameters.getArgs().contains("download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        String from = ResponseUtils.substringBetween(parameters.getArgs(), "from=", "&to=");
        String to = ResponseUtils.substringAfter(parameters.getArgs(), "&to=");

        writeResponse(response, new LogDownloader().getSortedExceptions(from, to), 200);
    }

    @Nullable
    private SessionInfo setSessionInfo(final HttpServletRequest request, String sessionId) {
        if (request.getSession().isNew()) {
            Statistics.incNumberOfUsers();
        }
        if (!sessionId.equals(request.getSession().getId()) && !sessionId.equals("-1")) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.info(ErrorWriter.getExceptionForLog("SET_SESSION_ID",
                    "Different id form request string and from servlet", sessionId + " " + request.getSession().getId()));
        }
        SessionInfo sessionInfo = new SessionInfo(request.getSession().getId());
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");

        if (userInfo == null) {
            userInfo = new UserInfo();
            request.getSession().setAttribute("userInfo", userInfo);
        }
        sessionInfo.setUserInfo(userInfo);
        return sessionInfo;
        /*String sessionIdFromCookies = getSessionIdFromCookies(request.getHeader("Cookie"));
        if (sessionIdFromCookies == null || sessionIdFromCookies.isEmpty()) {
            sessionIdFromCookies = sessionId;
            if (!sessionIdFromCookies.equals("")) {
                sessionInfo = new SessionInfo(Integer.parseInt(sessionIdFromCookies), ip);
                if (request.getHeader("Cookie") == null) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.info(ErrorWriter.getExceptionForLog("SET_SESSION_ID",
                            "Impossible to read id from cookies", sessionIdFromCookies));
                } else {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.info(ErrorWriter.getExceptionForLog("SET_SESSION_ID",
                            "Impossible to read id from cookies", request.getHeader("Cookie")));
                }
            } else {
                Statistics.incNumberOfUsers();
                sessionInfo = new SessionInfo(Integer.parseInt(Statistics.getNumberOfUsers()), ip);
                ErrorWriterOnServer.LOG_FOR_INFO.info("Number_of_users_since_start_server : " + Statistics.getNumberOfUsers() + " ip=" + ip);
            }
        } else {
            try {
                sessionInfo = new SessionInfo(Integer.parseInt(sessionIdFromCookies), ip);
            } catch (NumberFormatException e) {
                Statistics.incNumberOfUsers();
                sessionInfo = new SessionInfo(Integer.parseInt(Statistics.getNumberOfUsers()), ip);
                ErrorWriterOnServer.LOG_FOR_INFO.info("Number_of_users_since_start_server : " + Statistics.getNumberOfUsers());
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.SEND_USER_DATA.toString(), e, sessionIdFromCookies));
            }
        }
        return sessionInfo;*/
    }

    /* private String getSessionIdFromRequest(String request) {
        if (request.contains("sessionId=")) {
            return ResponseUtils.substringBetween(request, "sessionId=", "&");
        }
        return "";
    }*/

    @Nullable
    private String getSessionIdFromCookies(@Nullable String cookie) {
        if (cookie != null) {
            String response = ResponseUtils.substringBetween(cookie, "userId=", ";");
            ErrorWriterOnServer.LOG_FOR_INFO.info("cookies:" + cookie + " userId=" + response);
            return response;
        } else {
            return null;
        }
    }

    private String generateStringFromList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s);
            builder.append(";");
        }
        return builder.toString();
    }

    private void sendHelpContentForExamples(final HttpServletResponse response) {
        writeResponse(response, HelpLoader.getInstance().getHelpForExamples(), 200);
    }

    private void sendHelpContentForWords(final HttpServletResponse response) {
        writeResponse(response, HelpLoader.getInstance().getHelpForWords(), 200);
    }

    private void sendLog(final HttpServletRequest request, final HttpServletResponse response, RequestParameters parameters) {
        String path;
        if (parameters.getArgs().contains("&download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        if (parameters.getArgs().contains("&view")) {
            path = ResponseUtils.substringBefore(parameters.getArgs(), "&view");
        } else {
            path = ResponseUtils.substringBefore(parameters.getArgs(), "&download");
        }
        path = path.replaceAll("%5C", "/");
        writeResponse(response, new LogDownloader().download(path), 200);
    }

    private void sendListLogs(final HttpServletRequest request, final HttpServletResponse response, boolean updateStatistics) {
        String responseStr = null;
        InputStream is = null;
        try {
            is = ServerHandler.class.getResourceAsStream("/logs.html");
            responseStr = ResponseUtils.readData(is, true);

        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "Exception until downloading logs.html"));
            writeResponse(response, "Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
            return;
        } finally {
            close(is);
        }

        String links = new LogDownloader().getFilesLinks();
        responseStr = responseStr.replace("$LINKSTOLOGFILES$", links);
        responseStr = responseStr.replace("$CURRENTDATE$", ResponseUtils.getDate(Calendar.getInstance()));

        if (updateStatistics) {
            Statistics.getInstance().updateStatistics(true);
        }
        responseStr = Statistics.getInstance().writeStatistics(responseStr);
        writeResponse(response, responseStr, 200);
    }

    private void sendExamplesList(final HttpServletResponse response) {
        ExamplesLoader loader = new ExamplesLoader();
        writeResponse(response, loader.getExamplesList(), HttpStatus.SC_OK);
    }

    private void sendUserInformation(final HttpServletRequest request, final HttpServletResponse response, SessionInfo info) {
        StringBuilder reqResponse = new StringBuilder();
        InputStream is = null;
        try {
            is = request.getInputStream();
            reqResponse.append(ResponseUtils.readData(is));
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), e, "null"));
            writeResponse(response, "Cannot read data from file", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        } finally {
            close(is);
        }
        try {
            reqResponse = new StringBuilder(URLDecoder.decode(reqResponse.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), e, "null"));
        }
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), info.getId(), SessionInfo.TypeOfRequest.SEND_USER_DATA.name()));
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), info.getId(), ResponseUtils.substringAfter(reqResponse.toString(), "text=")));
        writeResponse(response, "OK", HttpStatus.SC_OK);
    }

    private void sendResourceFile(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI() + "?" + request.getQueryString();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");
        ErrorWriterOnServer.LOG_FOR_INFO.error(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "-1", "Resource doesn't downloaded from nginx: " + path));
        if (path.equals("")) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Path to the file is incorrect.", path));
            writeResponse(response, "Path to the file is incorrect.", HttpStatus.SC_NOT_FOUND);
            return;
        } else if (path.startsWith("/messages/")) {
            writeResponse(response, "", HttpStatus.SC_OK);
            return;
        } else if (path.equals("/") || path.equals("/index.html")) {
            path = "/index.html";
            StringBuilder responseStr = new StringBuilder();
            InputStream is = null;
            try {
                is = ServerHandler.class.getResourceAsStream(path);
                responseStr.append(ResponseUtils.readData(is, true));
            } catch (FileNotFoundException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "index.html not found"));
                writeResponse(response, "Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
                return;
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "Exception until downloading index.html"));
                writeResponse(response, "Cannot open this page", HttpStatus.SC_BAD_GATEWAY);
                return;
            } finally {
                close(is);
            }

            OutputStream os = null;
            try {
                os = response.getOutputStream();
                os.write(responseStr.toString().getBytes());
            } catch (IOException e) {
                //This is an exception we can't send data to client
            } finally {
                close(os);
            }
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Resource not found.", request.getRequestURI() + "?" + request.getQueryString()));
            writeResponse(response, ("Resource not found. " + path), HttpStatus.SC_NOT_FOUND);
            return;
        }

        try {
            ByteStreams.copy(is, response.getOutputStream());
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, request.getRequestURI() + "?" + request.getQueryString()));
            writeResponse(response, "Could not load the resource from the server", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void writeResponse(final HttpServletResponse response, String responseBody, int errorCode) {
        PrintWriter writer = null;
        try {
//            exchange.sendResponseHeaders(errorCode, responseBody.length);
            response.setStatus(errorCode);
            writer = response.getWriter();
            writer.write(responseBody);
        } catch (IOException e) {
            //This is an exception we can't to send data to client
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("UNKNOWN", e, ""));
//            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("UNKNOWN", e, response.getRequestURI().toString()));
        } finally {
            close(writer);
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

