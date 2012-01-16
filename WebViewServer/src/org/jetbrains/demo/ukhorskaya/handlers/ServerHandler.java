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

public class ServerHandler implements HttpHandler {
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

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        String ip = exchange.getRemoteAddress().getAddress().getHostAddress();

        SessionInfo sessionInfo;
        try {
            String param = exchange.getRequestURI().toString();

            if (param.contains("userData=true")) {
                sessionInfo = setSessionInfo(exchange);
                sendUserInformation(exchange, sessionInfo);
            } else if (param.contains("getSessionId")) {
                sessionInfo = setSessionInfo(exchange);
                String id = String.valueOf(sessionInfo.getId());
                exchange.sendResponseHeaders(HttpStatus.SC_OK, id.length());
                OutputStream out = null;
                try {
                    out = exchange.getResponseBody();
                    out.write(id.getBytes());
                } catch (Throwable e) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(exchange.getRequestURI().toString(), e, "null"));
                } finally {
                    close(out);
                }
            } else if (param.startsWith("/updateExamples")) {
                updateExamples(exchange);
            } else if (param.startsWith("/logs")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendListLogs(exchange);
            } else if (param.equals("/showUserInfo=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendUserInfoForStatistics(exchange);
            } else if (param.contains("/sortedExceptions=")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name());
                sendSortedExceptions(exchange);
            } else if (param.contains("log=")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name() + " " + exchange.getRequestURI());
                sendLog(exchange);
            } else if (param.contains("allExamples=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                sendExamplesList(exchange);
            } else if (param.contains("allHelpExamples=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_EXAMPLES.name());
                sendHelpContentForExamples(exchange);
            } else if (param.contains("allHelpWords=true")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_WORDS.name());
                sendHelpContentForWords(exchange);
            } else if ((param.startsWith("/?"))
                    || param.contains("testConnection")
                    || param.contains("writeLog=")) {
                if (!param.contains("testConnection") && !param.contains("writeLog=") && !ip.equals("127.0.0.1")) {
                    sessionInfo = setSessionInfo(exchange);
                } else {
                    sessionInfo = new SessionInfo(0, ip);
                }
                HttpSession session = new HttpSession(sessionInfo);
                session.handle(exchange);
            } else {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_RESOURCE.name() + " " + exchange.getRequestURI());
                sendResourceFile(exchange);
            }
        } catch (Throwable e) {
            //Do not stop server
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(exchange.getRequestURI().toString(), e, "null"));
            writeResponse(exchange, "Internal server error".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendUserInfoForStatistics(HttpExchange exchange) {
        writeResponse(exchange, Statistics.getInstance().showMap().getBytes(), HttpStatus.SC_OK);
    }


    private void updateExamples(HttpExchange exchange) {
        String response = ExamplesList.updateList();
        response += HelpLoader.updateExamplesHelp();
        writeResponse(exchange, response.getBytes(), HttpStatus.SC_OK);
    }

    private void sendSortedExceptions(HttpExchange exchange) {
        String param = exchange.getRequestURI().toString();
        if (param.contains("download")) {
            exchange.getResponseHeaders().add("Content-type", "application/x-download");
        }
        String from = ResponseUtils.substringBetween(param, "&from=", "&to=");
        String to = ResponseUtils.substringAfter(param, "&to=");

        writeResponse(exchange, new LogDownloader().getSortedExceptions(from, to).getBytes(), 200);
    }

    @Nullable
    private SessionInfo setSessionInfo(HttpExchange exchange) {
        SessionInfo sessionInfo;
        String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        String sessionId = getSessionIdFromCookies(exchange.getRequestHeaders());
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = getSessionIdFromRequest(exchange.getRequestURI().toString());
            if (!sessionId.equals("")) {
                sessionInfo = new SessionInfo(Integer.parseInt(sessionId), ip);

                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.info(ErrorWriter.getExceptionForLog("SET_SESSION_ID",
                        "Impossible to read id from cookies", generateStringFromList(exchange.getRequestHeaders().get("Cookie"))));
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

    private String getSessionIdFromCookies(Headers responseHeaders) {
        for (String key : responseHeaders.keySet()) {
            if (key.equals("Cookie")) {
                List<String> cookie = responseHeaders.get(key);
                if (cookie.size() > 0) {
                    String all = generateStringFromList(cookie);
                    String response = ResponseUtils.substringBetween(all, "userId=", ";");
                    ErrorWriterOnServer.LOG_FOR_INFO.info("cookies:" + all + " userId: " + response);
                    return response;
                }
            }
        }
        return "";
    }

    private String generateStringFromList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s);
            builder.append(";");
        }
        return builder.toString();
    }

    private void sendHelpContentForExamples(HttpExchange exchange) {
        writeResponse(exchange, HelpLoader.getInstance().getHelpForExamples().getBytes(), 200);
    }

    private void sendHelpContentForWords(HttpExchange exchange) {
        writeResponse(exchange, HelpLoader.getInstance().getHelpForWords().getBytes(), 200);
    }

    private void sendLog(HttpExchange exchange) {
        String param = exchange.getRequestURI().toString();
        String path;
        if (param.contains("&download")) {
            exchange.getResponseHeaders().add("Content-type", "application/x-download");
        }
        if (param.contains("&view")) {
            path = ResponseUtils.substringBetween(param, "log=", "&view");
        } else {
            path = ResponseUtils.substringBetween(param, "log=", "&download");
        }
        path = path.replaceAll("%5C", "/");
        writeResponse(exchange, new LogDownloader().download(path).getBytes(), 200);
    }

    private void sendListLogs(HttpExchange exchange) {
        InputStream is = ServerHandler.class.getResourceAsStream("/logs.html");
        try {
            String response = ResponseUtils.readData(is);
            String links = new LogDownloader().getFilesLinks();
            response = response.replace("$LINKSTOLOGFILES$", links);
            response = response.replace("$CURRENTDATE$", ResponseUtils.getDate(Calendar.getInstance()));

            if (exchange.getRequestURI().toString().contains("&statistics")) {
                Statistics.getInstance().updateStatistics(true);
            }
            response = Statistics.getInstance().writeStatistics(response);
            writeResponse(exchange, response.getBytes(), 200);
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "null"));
        } finally {
            close(is);
        }
    }

    private void sendExamplesList(HttpExchange exchange) {
        ExamplesLoader loader = new ExamplesLoader();
        writeResponse(exchange, loader.getExamplesList().getBytes(), HttpStatus.SC_OK);
    }

    private void sendUserInformation(HttpExchange exchange, SessionInfo info) {
        StringBuilder reqResponse = new StringBuilder();
        InputStream is = null;
        try {
            is = exchange.getRequestBody();
            reqResponse.append(ResponseUtils.readData(is));
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), e, "null"));
            writeResponse(exchange, "Cannot read data from file".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
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
        writeResponse(exchange, "OK".getBytes(), HttpStatus.SC_OK);
    }

    private void sendResourceFile(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");

        if (path.equals("")) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Path to the file is incorrect.", exchange.getRequestURI().toString()));
            writeResponse(exchange, "Path to the file is incorrect.".getBytes(), HttpStatus.SC_NOT_FOUND);
            return;
        } else if (path.startsWith("/messages/")) {
            writeResponse(exchange, "".getBytes(), HttpStatus.SC_OK);
            return;
        } else if (path.equals("/")) {
            path = "/header.html";
        } else if (path.equals("/WebViewApplet.jar")) {
            OutputStream responseBodyBytes = null;
            InputStream is = null;
            try {
                is = ServerHandler.class.getResourceAsStream("/WebViewApplet.jar");
                if (is == null) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                            SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Impossible to load applet", exchange.getRequestURI().toString()));
                    writeResponse(exchange, "Impossible to load applet".getBytes(), HttpStatus.SC_OK);
                    return;
                }
                int size = APPLET_FILE.length - (APPLET_FILE.length / 4096) * 4096;

                exchange.sendResponseHeaders(HttpStatus.SC_OK, APPLET_FILE.length);
                responseBodyBytes = exchange.getResponseBody();
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
                            SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Impossible to load applet", exchange.getRequestURI().toString()));
                    writeResponse(exchange, "Broken applet: incorrect applet size".getBytes(), HttpStatus.SC_OK);
                    return;
                }

            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, exchange.getRequestURI().toString()));
                writeResponse(exchange, "Impossible to load applet".getBytes(), HttpStatus.SC_OK);
                return;
            } finally {
                close(responseBodyBytes);
                close(is);
                exchange.close();
            }
//            writeResponse(exchange, APPLET_FILE, HttpStatus.SC_OK);
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Resource not found.", exchange.getRequestURI().toString()));
            writeResponse(exchange, ("Resource not found. " + path).getBytes(), HttpStatus.SC_NOT_FOUND);
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
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, exchange.getRequestURI().toString()));
            writeResponse(exchange, "Could not load the resource from the server".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        writeResponse(exchange, out.toByteArray(), HttpStatus.SC_OK);
    }

    private void writeResponse(HttpExchange exchange, byte[] responseBody, int errorCode) {
        OutputStream os = null;
        try {
            exchange.sendResponseHeaders(errorCode, responseBody.length);
            os = exchange.getResponseBody();
            os.write(responseBody);
        } catch (IOException e) {
            //This is an exception we can't to send data to client
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("UNKNOWN", e, exchange.getRequestURI().toString()));
        } finally {
            close(os);
            exchange.close();
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

