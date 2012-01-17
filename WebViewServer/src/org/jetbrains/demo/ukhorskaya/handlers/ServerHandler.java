package org.jetbrains.demo.ukhorskaya.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.Statistics;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesList;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesLoader;
import org.jetbrains.demo.ukhorskaya.help.HelpLoader;
import org.jetbrains.demo.ukhorskaya.log.LogDownloader;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.demo.ukhorskaya.sessions.HttpSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/10/11
 * Time: 1:16 PM
 */

public class ServerHandler {
    private final byte[] APPLET_FILE;

    public ServerHandler() {
        InputStream is = ServerHandler.class.getResourceAsStream("/WebViewApplet.jar");
        if (is == null) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Resource not found.", "WebViewApplet.jar"));
            APPLET_FILE = new byte[0];
            return;
        }

        int length;
        byte[] tmp = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            while ((length = is.read(tmp)) >= 0) {
                out.write(tmp, 0, length);
            }
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, "WebViewApplet.jar"));
        } finally {
            close(out);
        }
        APPLET_FILE = out.toByteArray();

    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String ip = request.getRemoteAddr();

        SessionInfo sessionInfo;
        try {
            String param = request.getQueryString();
            if (param == null) {
                param = request.getRequestURI();
            }
            if (param.contains("userData=true")) {
                sessionInfo = setSessionInfo(request);
                sendUserInformation(request, response, sessionInfo);
            } else if (param.contains("getSessionId")) {
                sessionInfo = setSessionInfo(request);
                String id = String.valueOf(sessionInfo.getId());
//                response.addHeader(HttpServletResponse.SC_OK);
//                response.sendResponseHeaders(HttpStatus.SC_OK, id.length());
                PrintWriter out = null;
                try {
                    out = response.getWriter();
                    out.write(id);
                } catch (Throwable e) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(request.getRequestURI(), e, "null"));
                } finally {
                    close(out);
                }
            } else if (param.startsWith("updateExamples")) {
                updateExamples(response);
            } else if (param.startsWith("logs")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendListLogs(request, response);
            } else if (param.equals("showUserInfo=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendUserInfoForStatistics(response);
            } else if (param.contains("sortedExceptions=")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name());
                sendSortedExceptions(request, response);
            } else if (param.contains("log=")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name() + " " + request.getRequestURI());
                sendLog(request, response);
            } else if (param.contains("allExamples=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                sendExamplesList(response);
            } else if (param.contains("allHelpExamples=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_EXAMPLES.name());
                sendHelpContentForExamples(response);
            } else if (param.contains("allHelpWords=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_WORDS.name());
                sendHelpContentForWords(response);
            } else if ((param.startsWith("?"))
                    || param.contains("sendData=true")
                    || param.contains("complete=true")
                    || param.contains("convert")
                    || param.contains("testConnection")
                    || param.contains("run=true")
                    || param.contains("writeLog=")) {
                if (!param.contains("testConnection") && !param.contains("writeLog=") && !ip.equals("127.0.0.1")) {
                    sessionInfo = setSessionInfo(request);
                } else {
                    sessionInfo = new SessionInfo(0, ip);
                }
                HttpSession session = new HttpSession(sessionInfo);
                session.handle(request, response);
            } else {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_RESOURCE.name() + " " + request.getRequestURI());
                sendResourceFile(request, response);
            }
        } catch (Throwable e) {
            //Do not stop server
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(request.getRequestURI(), e, "null"));
            writeResponse(response, "Internal server error", HttpStatus.SC_INTERNAL_SERVER_ERROR);
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

    private void sendSortedExceptions(final HttpServletRequest request, final HttpServletResponse response) {
        String param = request.getRequestURI();
        if (param.contains("download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        String from = ResponseUtils.substringBetween(param, "&from=", "&to=");
        String to = ResponseUtils.substringAfter(param, "&to=");

        writeResponse(response, new LogDownloader().getSortedExceptions(from, to), 200);
    }

    @Nullable
    private SessionInfo setSessionInfo(final HttpServletRequest request) {
        SessionInfo sessionInfo;
        String ip = request.getRemoteAddr();
        String sessionId = getSessionIdFromCookies(request.getHeader("Cookie"));
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = getSessionIdFromRequest(request.getRequestURI());
            if (!sessionId.equals("")) {
                sessionInfo = new SessionInfo(Integer.parseInt(sessionId), ip);

                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.info(ErrorWriter.getExceptionForLog("SET_SESSION_ID",
                        "Impossible to read id from cookies", request.getHeader("Cookie")));
            } else {
                Statistics.incNumberOfUsers();
                sessionInfo = new SessionInfo(Integer.parseInt(Statistics.getNumberOfUsers()), ip);
                ErrorWriterOnServer.LOG_FOR_INFO.info("Number_of_users_since_start_server : " + Statistics.getNumberOfUsers() + " ip=" + ip);
            }
        } else {
            try {
                sessionInfo = new SessionInfo(Integer.parseInt(sessionId), ip);
            } catch (NumberFormatException e) {
                Statistics.incNumberOfUsers();
                sessionInfo = new SessionInfo(Integer.parseInt(Statistics.getNumberOfUsers()), ip);
                ErrorWriterOnServer.LOG_FOR_INFO.info("Number_of_users_since_start_server : " + Statistics.getNumberOfUsers());
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.SEND_USER_DATA.toString(), e, sessionId));
            }
        }
        return sessionInfo;
    }

    private String getSessionIdFromRequest(String request) {
        if (request.contains("sessionId=")) {
            return ResponseUtils.substringBetween(request, "sessionId=", "&");
        }
        return "";
    }

    private String getSessionIdFromCookies(String cookie) {
        String response = ResponseUtils.substringBetween(cookie, "userId=", ";");
        ErrorWriterOnServer.LOG_FOR_INFO.info("cookies:" + cookie + " userId= " + response);
        return response;
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

    private void sendLog(final HttpServletRequest request, final HttpServletResponse response) {
        String param = request.getRequestURI();
        String path;
        if (param.contains("&download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        if (param.contains("&view")) {
            path = ResponseUtils.substringBetween(param, "log=", "&view");
        } else {
            path = ResponseUtils.substringBetween(param, "log=", "&download");
        }
        path = path.replaceAll("%5C", "/");
        writeResponse(response, new LogDownloader().download(path), 200);
    }

    private void sendListLogs(final HttpServletRequest request, final HttpServletResponse response) {
        InputStream is = ServerHandler.class.getResourceAsStream("/logs.html");
        try {
            String responseStr = ResponseUtils.readData(is);
            String links = new LogDownloader().getFilesLinks();
            responseStr = responseStr.replace("$LINKSTOLOGFILES$", links);
            responseStr = responseStr.replace("$CURRENTDATE$", ResponseUtils.getDate(Calendar.getInstance()));

            if (request.getRequestURI().contains("&statistics")) {
                Statistics.getInstance().updateStatistics(true);
            }
            responseStr = Statistics.getInstance().writeStatistics(responseStr);
            writeResponse(response, responseStr, 200);
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "null"));
        } finally {
            close(is);
        }
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
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), info.getId(), info.getIp(), SessionInfo.TypeOfRequest.SEND_USER_DATA.name()));
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), info.getId(), ResponseUtils.substringAfter(reqResponse.toString(), "text=")));
        writeResponse(response, "OK", HttpStatus.SC_OK);
    }

    private void sendResourceFile(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");

        if (path.equals("")) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Path to the file is incorrect.", request.getRequestURI()));
            writeResponse(response, "Path to the file is incorrect.", HttpStatus.SC_NOT_FOUND);
            return;
        } else if (path.startsWith("/messages/")) {
            writeResponse(response, "", HttpStatus.SC_OK);
            return;
        } else if (path.equals("/WebDemo/")) {
            path = "/header.html";
        } else if (path.equals("/WebViewApplet.jar")) {
            OutputStream responseBodyBytes = null;
            InputStream is = null;
            try {
                is = ServerHandler.class.getResourceAsStream("/WebViewApplet.jar");
                if (is == null) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                            SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Impossible to load applet", request.getRequestURI()));
                    writeResponse(response, "Impossible to load applet", HttpStatus.SC_OK);
                    return;
                }
                int size = APPLET_FILE.length - (APPLET_FILE.length / 4096) * 4096;

                response.setStatus(HttpStatus.SC_OK);
//                response.sendResponseHeaders(HttpStatus.SC_OK, APPLET_FILE.length);
                responseBodyBytes = response.getOutputStream();

                int read = 0;
                int totalRead = 0;
                byte[] buffer = new byte[4096];
                byte[] lastBuffer;
                while ((read = is.read(buffer)) >= 0) {
                    /* if (read == size) {
                      lastBuffer = Arrays.copyOf(buffer, size);
                      responseBodyBytes.write(lastBuffer);
                  } else {*/
                    responseBodyBytes.write(buffer, 0, read);
//                    }
                    totalRead += read;
                }
                if (totalRead != APPLET_FILE.length) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                            SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Impossible to load applet", request.getRequestURI()));
                    writeResponse(response, "Broken applet: incorrect applet size", HttpStatus.SC_OK);
                    return;
                }

            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, request.getRequestURI()));
                writeResponse(response, "Impossible to load applet", HttpStatus.SC_OK);
                return;
            } finally {
                close(responseBodyBytes);
                close(is);
            }
//            writeResponse(exchange, APPLET_FILE, HttpStatus.SC_OK);
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Resource not found.", request.getRequestURI()));
            writeResponse(response, ("Resource not found. " + path), HttpStatus.SC_NOT_FOUND);
            return;
        }

        int length;
        byte[] tmp = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            while ((length = is.read(tmp)) >= 0) {
                out.write(tmp, 0, length);
            }
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, request.getRequestURI()));
            writeResponse(response, "Could not load the resource from the server", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try {
            out.writeTo(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        writeResponse(response, out., HttpStatus.SC_OK);
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

