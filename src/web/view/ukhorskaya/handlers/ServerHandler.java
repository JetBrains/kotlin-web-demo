package web.view.ukhorskaya.handlers;

import com.intellij.openapi.application.Application;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import web.view.ukhorskaya.ApplicationErrorsWriter;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.examplesLoader.ExamplesLoader;
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
    private static final Logger LOG = Logger.getLogger(ServerHandler.class);

    public static int numberOfUsers = 0;

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            String param = exchange.getRequestURI().toString();

            if (param.contains("userData=true")) {
                sendUserInformation(exchange);
                throw new NullPointerException("sadasdasdasd");
            } else if (param.contains("downloadLogs=true")) {
                sendListLogs(exchange);
            } else if (param.contains("log=")) {
                sendLog(exchange);
            } else if (param.contains("allExamples=true")) {
                sendExamplesList(exchange);
            } else if ((param.contains("/editor"))
                    || (param.contains("/path="))
                    || (param.equals("/"))
                    || (param.startsWith("/?"))) {
                HttpSession session = new HttpSession();
                session.handle(exchange);
            } else {
                sendResourceFile(exchange);
            }
        } catch (Throwable e) {
            //Do not stop server
//            LOG.error("Unknown error", e);
            ApplicationErrorsWriter.LOG_FOR_EXCEPTIONS.error(ApplicationErrorsWriter.getExceptionForLog(exchange.getRequestURI().toString(), e.getMessage(), "null"), e);
            writeResponse(exchange, "Internal server error".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendLog(HttpExchange exchange) {
        String path = ResponseUtils.substringAfter(exchange.getRequestURI().toString(), "log=");
        path = path.replaceAll("%5C", "/");
        exchange.getResponseHeaders().add("Content-type", "application/x-download");
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
            e.printStackTrace();
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
            LOG.error("Cannot read data from file", e);
            writeResponse(exchange, "Cannot read data from file".getBytes(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        try {
            reqResponse = new StringBuilder(URLDecoder.decode(reqResponse.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Impossible to write to file in UTF-8");
        }
        LOG.info("User info: " + ResponseUtils.substringAfter(reqResponse.toString(), "text=") + " " + exchange.getRequestURI());
        writeResponse(exchange, "OK".getBytes(), HttpStatus.SC_OK);
    }

    private void sendResourceFile(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");

        if (path.equals("")) {
            LOG.error("Path to the file is incorrect: " + exchange.getRequestURI());
            writeResponse(exchange, "Path to the file is incorrect.".getBytes(), HttpStatus.SC_NOT_FOUND);
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            LOG.error("Resource not found: " + exchange.getRequestURI());
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
            LOG.error("Could not load the resource from the server.", e);
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
            LOG.info("resource request " + exchange.getRequestURI());
        } catch (IOException e) {
            //This is an exception we can't to send data to client
            LOG.error(e.getMessage(), e);
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
            LOG.error(e);
        }
    }
}

