package web.view.ukhorskaya.server;

import com.sun.net.httpserver.HttpServer;
import web.view.ukhorskaya.ApplicationErrorsWriter;
import web.view.ukhorskaya.handlers.ServerHandler;

import java.net.BindException;
import java.net.InetSocketAddress;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/5/11
 * Time: 10:05 AM
 */
public class KotlinHttpServer {
    private static final KotlinHttpServer myServer = new KotlinHttpServer();
    private static HttpServer server;

    public static KotlinHttpServer getInstance() {
        return myServer;
    }

    private ServerHandler myHandler;

    private static boolean isServerRunning = false;

    private KotlinHttpServer() {
    }

    public void startServer() {
        try {
            if (!isServerRunning) {
                server = HttpServer.create(new InetSocketAddress(ServerSettings.HOSTNAME, ServerSettings.SERVER_PORT), 10);
                if (myHandler == null) {
                    myHandler = new ServerHandler();
                }
                server.createContext("/", myHandler);
                server.setExecutor(null);
                server.start();
                isServerRunning = true;
                ApplicationErrorsWriter.writeInfoToConsole("Server is started");
            } else {
                ApplicationErrorsWriter.writeErrorToConsole("Server is already running. Use \"stop\" or \"restart\" commands.");
            }
        } catch (BindException e) {
            ApplicationErrorsWriter.writeExceptionToConsole("Address already in use. Use \"set hostname\" command to change it.", e);
        } catch (Exception e) {
            ApplicationErrorsWriter.writeExceptionToConsole("Server didn't start. Use \"start\" to try start server again.", e);
        }
    }

    public static void stopServer() {
        if (isServerRunning) {
            KotlinHttpServer.server.stop(0);
            ApplicationErrorsWriter.writeInfoToConsole("Server is stopped");
            isServerRunning = false;
        } else {
            ApplicationErrorsWriter.writeErrorToConsole("Server is not running");
        }
    }

    public static boolean isServerRunning() {
        return isServerRunning;
    }

}
