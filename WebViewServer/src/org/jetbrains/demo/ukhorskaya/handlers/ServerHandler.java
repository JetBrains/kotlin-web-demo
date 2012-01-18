package org.jetbrains.demo.ukhorskaya.handlers;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.*;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesList;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesLoader;
import org.jetbrains.demo.ukhorskaya.help.HelpLoader;
import org.jetbrains.demo.ukhorskaya.log.LogDownloader;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.demo.ukhorskaya.sessions.HttpSession;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/10/11
 * Time: 1:16 PM
 */

public class ServerHandler implements HttpHandler {

    public ServerHandler() {

    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        String ip = exchange.getRemoteAddress().getAddress().getHostAddress();

        SessionInfo sessionInfo;
        try {
            RequestParameters parameters = RequestParameters.parseRequest(exchange.getRequestURI().toString());
            if (exchange.getRequestURI().toString().startsWith("/kotlinServer&example=")) {
                 sendExampleByName(exchange);
            } else if (parameters.compareType("sendUserData")) {
                sessionInfo = setSessionInfo(exchange, parameters.getSessionId());
                sendUserInformation(exchange, sessionInfo);
            } else if (parameters.compareType("getSessionId")) {
                sessionInfo = setSessionInfo(exchange, parameters.getSessionId());
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
            } else if (parameters.compareType("updateExamples")) {
                updateExamples(exchange);
            } else if (exchange.getRequestURI().toString().startsWith("/logs") || parameters.compareType("updateStatistics")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                if (parameters.compareType("updateStatistics")) {
                    sendListLogs(exchange, true);
                } else {
                    sendListLogs(exchange, false);
                }
            } else if (parameters.compareType("showUserInfo")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendUserInfoForStatistics(exchange);
            } else if (parameters.compareType("sortExceptions")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name());
                sendSortedExceptions(exchange, parameters);
            } else if (parameters.compareType("downloadLog")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name() + " " + exchange.getRequestURI());
                sendLog(exchange, parameters);
            } else if (parameters.compareType("loadExample") && parameters.getArgs().equals("all")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                sendExamplesList(exchange);
            } else if (parameters.compareType("loadHelpForExamples")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_EXAMPLES.name());
                sendHelpContentForExamples(exchange);
            } else if (parameters.compareType("loadHelpForWords")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_WORDS.name());
                sendHelpContentForWords(exchange);
            } else if (parameters.compareType("highlight")
                    || parameters.compareType("complete")
                    || parameters.compareType("run")
                    || parameters.compareType("convertToJs")
                    || parameters.compareType("loadExample")
                    || parameters.compareType("writeLog")) {
                if (!parameters.compareType("writeLog") && !ip.equals("127.0.0.1")) {
                    sessionInfo = setSessionInfo(exchange, parameters.getSessionId());
                } else {
                    sessionInfo = new SessionInfo(0, ip);
                }
                HttpSession session = new HttpSession(sessionInfo, parameters);
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

    private void sendExampleByName(HttpExchange exchange) {
        String exampleName = ResponseUtils.substringAfter(exchange.getRequestURI().toString(), "example=");
        ExamplesLoader loader = new ExamplesLoader();
        writeResponse(exchange, loader.getResultByExampleName(exampleName).getBytes(), HttpStatus.SC_OK);
    }

    private void sendUserInfoForStatistics(HttpExchange exchange) {
        writeResponse(exchange, Statistics.getInstance().showMap().getBytes(), HttpStatus.SC_OK);
    }


    private void updateExamples(HttpExchange exchange) {
        String response = ExamplesList.updateList();
        response += HelpLoader.updateExamplesHelp();
        writeResponse(exchange, response.getBytes(), HttpStatus.SC_OK);
    }

    private void sendSortedExceptions(HttpExchange exchange, RequestParameters parameters) {
        if (parameters.getArgs().contains("download")) {
            exchange.getResponseHeaders().add("Content-type", "application/x-download");
        }
        String from = ResponseUtils.substringBetween(parameters.getArgs(), "from=", "&to=");
        String to = ResponseUtils.substringAfter(parameters.getArgs(), "&to=");

        writeResponse(exchange, new LogDownloader().getSortedExceptions(from, to).getBytes(), 200);
    }

    @Nullable
    private SessionInfo setSessionInfo(HttpExchange exchange, String sessionId) {
        SessionInfo sessionInfo;
        String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        String sessionIdFromCookies = getSessionIdFromCookies(exchange.getRequestHeaders());
        if (sessionIdFromCookies == null || sessionIdFromCookies.isEmpty()) {
            sessionIdFromCookies = sessionId;
            if (!sessionIdFromCookies.equals("")) {
                sessionInfo = new SessionInfo(Integer.parseInt(sessionIdFromCookies), ip);

                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.info(ErrorWriter.getExceptionForLog("SET_SESSION_ID",
                        "Impossible to read id from cookies", generateStringFromList(exchange.getRequestHeaders().get("Cookie"))));
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

    private void sendLog(HttpExchange exchange, RequestParameters parameters) {
        String path;
        if (parameters.getArgs().contains("&download")) {
            exchange.getResponseHeaders().add("Content-type", "application/x-download");
        }
        if (parameters.getArgs().contains("&view")) {
            path = ResponseUtils.substringBefore(parameters.getArgs(), "&view");
        } else {
            path = ResponseUtils.substringBefore(parameters.getArgs(), "&download");
        }
        path = path.replaceAll("%5C", "/");
        writeResponse(exchange, new LogDownloader().download(path).getBytes(), 200);
    }

    private void sendListLogs(HttpExchange exchange, boolean updateStatistics) {
        String response = null;
        try {
            response = Files.toString(new File(ServerSettings.RESOURCES_ROOT + File.separator + "logs.html"), Charset.forName("UTF-8"));
        } catch (FileNotFoundException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "logs.html not found"));
            writeResponse(exchange, "Cannot open this page".getBytes(), HttpStatus.SC_BAD_GATEWAY);
            return;
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "Exception until downloading logs.html"));
            writeResponse(exchange, "Cannot open this page".getBytes(), HttpStatus.SC_BAD_GATEWAY);
            return;
        }

        String links = new LogDownloader().getFilesLinks();
        response = response.replace("$LINKSTOLOGFILES$", links);
        response = response.replace("$CURRENTDATE$", ResponseUtils.getDate(Calendar.getInstance()));

        if (updateStatistics) {
            Statistics.getInstance().updateStatistics(true);
        }
        response = Statistics.getInstance().writeStatistics(response);
        writeResponse(exchange, response.getBytes(), 200);

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
        } else if (path.equals("/") || path.equals("/index.html")) {
            path = "/index.html";
            StringBuilder response = new StringBuilder();
            try {
                response.append(Files.toString(new File(ServerSettings.RESOURCES_ROOT + File.separator + path), Charset.forName("UTF-8")));
            } catch (FileNotFoundException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "index.html not found"));
                writeResponse(exchange, "Cannot open this page".getBytes(), HttpStatus.SC_BAD_GATEWAY);
                return;
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), e, "Exception until downloading index.html"));
                writeResponse(exchange, "Cannot open this page".getBytes(), HttpStatus.SC_BAD_GATEWAY);
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
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "Resource not found.", exchange.getRequestURI().toString()));
            writeResponse(exchange, ("Resource not found. " + path).getBytes(), HttpStatus.SC_NOT_FOUND);
            return;
        }

        try {
            exchange.sendResponseHeaders(HttpStatus.SC_OK, 0);
            ByteStreams.copy(is, exchange.getResponseBody());
            exchange.close();
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), e, exchange.getRequestURI().toString()));
            writeResponse(exchange, "Could not load the resource from the server".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

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

