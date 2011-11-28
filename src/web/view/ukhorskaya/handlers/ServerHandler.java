package web.view.ukhorskaya.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.examplesLoader.ExamplesLoader;
import web.view.ukhorskaya.help.HelpLoader;
import web.view.ukhorskaya.log.LogDownloader;
import web.view.ukhorskaya.sessions.HttpSession;

import java.io.*;
import java.net.URLDecoder;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/10/11
 * Time: 1:16 PM
 */

public class ServerHandler implements HttpHandler {
//    private static final Logger LOG = Logger.getLogger(ServerHandler.class);

    public static int numberOfUsers = 0;

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            String param = exchange.getRequestURI().toString();

            if (param.contains("userData=true")) {
                sendUserInformation(exchange);
            } else if (param.equals("/logs")) {
                ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.GET_LOGS_LIST.name());
                sendListLogs(exchange);
            } else if (param.contains("log=")) {
                ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.DOWNLOAD_LOG.name() + " " + exchange.getRequestURI());
                sendLog(exchange);
            } else if (param.contains("allExamples=true")) {
                ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.GET_EXAMPLES_LIST.name());
                sendExamplesList(exchange);
            } else if (param.contains("allHelpExamples=true")) {
                ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.GET_HELP_FOR_EXAMPLES.name());
                sendHelpContentForExamples(exchange);
            } else if (param.contains("allHelpWords=true")) {
                ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.GET_HELP_FOR_WORDS.name());
                sendHelpContentForWords(exchange);
            } else if ((param.contains("/editor"))
                    || (param.contains("/path="))
                    || (param.equals("/"))
                    || (param.startsWith("/?"))) {
                HttpSession session = new HttpSession();
                session.handle(exchange);
            } else {
                ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.GET_RESOURCE.name() + " " + exchange.getRequestURI());
                sendResourceFile(exchange);
            }
        } catch (Throwable e) {
            //Do not stop server
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(exchange.getRequestURI().toString(), e, "null"));
            writeResponse(exchange, "Internal server error".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
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
        if (param.contains("&view")) {
            path = ResponseUtils.substringBetween(param, "log=", "&view");
        } else {
            path = ResponseUtils.substringBetween(param, "log=", "&download");
        }
        path = path.replaceAll("%5C", "/");
        if (param.contains("&dowmload")) {
            exchange.getResponseHeaders().add("Content-type", "application/x-download");
        }
        writeResponse(exchange, new LogDownloader().download(path).getBytes(), 200);
    }

    private void sendListLogs(HttpExchange exchange) {
        InputStream is = ServerHandler.class.getResourceAsStream("/logs.html");
        try {
            String response = ResponseUtils.readData(is);
            String links = new LogDownloader().getFilesLinks();
            response = response.replace("$LINKSTOLOGFILES$", links);
            writeResponse(exchange, response.getBytes(), 200);
        } catch (IOException e) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.GET_LOGS_LIST.name(), e, "null"));
        }
    }

    private void sendExamplesList(HttpExchange exchange) {
        ExamplesLoader loader = new ExamplesLoader();
        writeResponse(exchange, loader.getExamplesList().getBytes(), HttpStatus.SC_OK);
    }

    private void sendUserInformation(HttpExchange exchange) {
        StringBuilder reqResponse = new StringBuilder();
        try {
            reqResponse.append(ResponseUtils.readData(exchange.getRequestBody()));
        } catch (IOException e) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.SEND_USER_DATA.name(), e, "null"));
            writeResponse(exchange, "Cannot read data from file".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        try {
            reqResponse = new StringBuilder(URLDecoder.decode(reqResponse.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.SEND_USER_DATA.name(), e, "null"));
        }
        ErrorsWriter.LOG_FOR_INFO.info(TypeOfRequest.SEND_USER_DATA.name() + " " + ResponseUtils.substringAfter(reqResponse.toString(), "text=") + " " + TypeOfRequest.SEND_USER_DATA.name());
        writeResponse(exchange, "OK".getBytes(), HttpStatus.SC_OK);
    }

    private void sendResourceFile(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");

        if (path.equals("")) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.GET_RESOURCE.name(), "Path to the file is incorrect.", exchange.getRequestURI().toString()));
            writeResponse(exchange, "Path to the file is incorrect.".getBytes(), HttpStatus.SC_NOT_FOUND);
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.GET_RESOURCE.name(), "Resource not found.", exchange.getRequestURI().toString()));
            writeResponse(exchange, "Resource not found.".getBytes(), HttpStatus.SC_NOT_FOUND);
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
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog(TypeOfRequest.GET_RESOURCE.name(), e, exchange.getRequestURI().toString()));
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
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog("UNKNOWN", e, exchange.getRequestURI().toString()));
        } finally {
            close(os);
        }
    }

    private void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog("UNKNOWN", e, " NULL"));
        }
    }

    public enum TypeOfRequest {
        HIGHLIGHT,
        COMPLETE,
        RUN,
        LOAD_EXAMPLE,
        SEND_USER_DATA,
        GET_LOGS_LIST,
        DOWNLOAD_LOG,
        GET_EXAMPLES_LIST,
        GET_HELP_FOR_EXAMPLES,
        GET_HELP_FOR_WORDS,
        GET_RESOURCE
    }

}

