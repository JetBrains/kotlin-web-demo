package web.view.ukhorskaya.handlers;

import com.intellij.openapi.diagnostic.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.rules.Timeout;
import web.view.ukhorskaya.sessions.HttpSession;

import javax.imageio.ImageIO;
import javax.management.timer.Timer;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/10/11
 * Time: 1:16 PM
 */

public class BaseHandler implements HttpHandler {
    private static final Logger LOG = Logger.getInstance(BaseHandler.class);

    @Override
    public void handle(final HttpExchange exchange) throws IOException {

        final long time = System.nanoTime();
        if (!sendNonSourceFile(exchange)) {
            HttpSession session = new HttpSession(time);
            session.handle(exchange);
        }
    }

    //Method send all data except source file
    protected boolean sendNonSourceFile(HttpExchange exchange) {

        String param = exchange.getRequestURI().getQuery();
        if (param != null) {
            if (param.contains("file=dialog.js")) {
                sendResourceFile(exchange);
                return true;
            } else if (param.contains("type=jquery_lib") || param.contains("type=css")) {
                sendResourceFile(exchange);
                return true;
            }
        } else {
            param = exchange.getRequestURI().toString();
            if (param.contains("fticons")) {
                System.out.println("WARN: restore iconHelper");
                sendIcon(exchange);
                return true;
            } else if ((param.contains(".png")) || (param.contains(".gif"))) {
                sendImageFile(exchange);
                return true;
            } else if (param.contains(".css") && (param.contains("jquery.ui") || param.contains("jquery-ui"))) {
                sendResourceFile(exchange);
                return true;
            }
        }
        return false;
    }

    private void sendIcon(HttpExchange exchange) {
        /*String hashCode = exchange.getRequestURI().getPath();
        hashCode = hashCode.substring(hashCode.indexOf("fticons/") + 8);
        BufferedImage bi = IconHelper.getInstance().getIconFromMap(Integer.parseInt(hashCode));
        writeImageToStream(bi, exchange);*/
    }

    private void sendImageFile(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        if (path.contains("resources")) {
            path = path.substring(path.indexOf("resources") + 9);
        }
        try {
            BufferedImage bi = ImageIO.read(BaseHandler.class.getResourceAsStream(path));
            writeImageToStream(bi, exchange);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private void writeImageToStream(BufferedImage image, HttpExchange exchange) {
        OutputStream out = null;
        try {
            //ByteArrayOutputStream exist because it is necessary to send headers before write to out
            ByteArrayOutputStream tmp = new ByteArrayOutputStream();
            ImageIO.write(image, "png", tmp);
            tmp.close();
            Integer contentLength = tmp.size();
            exchange.sendResponseHeaders(HttpStatus.SC_OK, contentLength);
            out = exchange.getResponseBody();
            out.write(tmp.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        } finally {
            close(out);
        }
    }

    private void sendResourceFile(HttpExchange exchange) {
        StringBuilder response = new StringBuilder();

        String path = exchange.getRequestURI().getPath();
        if (path.contains("resources")) {
            path = path.substring(path.indexOf("resources") + 9);
        }

        InputStream is = BaseHandler.class.getResourceAsStream(path);
        if (is == null) {
            writeResponse(exchange, "File not found", HttpStatus.SC_NOT_FOUND);
            return;
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                response.append(tmp);
                response.append("\n");
            }
        } catch (IOException e) {
            response.append("Error while reading from file");
            writeResponse(exchange, response.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        writeResponse(exchange, response.toString(), HttpStatus.SC_OK);
    }

    //Send Response
    private void writeResponse(HttpExchange exchange, String responseBody, int errorCode) {
        OutputStream os = null;
        StringBuilder response = new StringBuilder();
        response.append(responseBody);
        try {
            exchange.sendResponseHeaders(errorCode, response.length());
            os = exchange.getResponseBody();
            os.write(response.toString().getBytes());
        } catch (IOException e) {
            //This is an exception we can't to send data to client
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

