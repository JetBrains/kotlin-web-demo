package web.view.ukhorskaya.server;

import com.sun.net.httpserver.HttpServer;
import web.view.ukhorskaya.ErrorsWriter;
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

    private static ServerHandler myHandler;

    private static boolean isServerRunning = false;

    private KotlinHttpServer() {
    }

    public static String getHost() {
        if (server != null) {
            return server.getAddress().getHostName();
        } else return null;
    }

    public static int getPort() {
        if (server != null) {
            return server.getAddress().getPort();
        } else return 0;
    }

    public static void startServer() {
        try {
            if (!isServerRunning) {
                if (ServerSettings.HOST.equals("localhost")) {
                    server = HttpServer.create(new InetSocketAddress(Integer.parseInt(ServerSettings.PORT)), 10);
                } else {
                    server = HttpServer.create(new InetSocketAddress(ServerSettings.HOST, Integer.parseInt(ServerSettings.PORT)), 10);
                }
                if (myHandler == null) {
                    myHandler = new ServerHandler();
                }
                server.createContext("/", myHandler);
                server.setExecutor(null);
                server.start();
                isServerRunning = true;
                ErrorsWriter.writeInfoToConsole("Server is started at " + KotlinHttpServer.getHost() + ":" + KotlinHttpServer.getPort());
            } else {
                ErrorsWriter.writeErrorToConsole("Server is already running at " + KotlinHttpServer.getHost() + ":" + KotlinHttpServer.getPort()
                        + ". Use \"stop\" or \"restart\" commands.");
            }
        } catch (BindException e) {
            ErrorsWriter.writeExceptionToConsole("Address already in use. Use \"set hostname\" command to change it.", e);
        } catch (Exception e) {
            ErrorsWriter.writeExceptionToConsole("Server didn't start. Use \"start\" to try start server again.", e);
        }
    }

    public static void stopServer() {
        if (isServerRunning) {
            KotlinHttpServer.server.stop(0);
            ErrorsWriter.writeInfoToConsole("Server is stopped at " + KotlinHttpServer.getHost() + ":" + KotlinHttpServer.getPort());
            isServerRunning = false;
        } else {
            ErrorsWriter.writeErrorToConsole("Server is not running");
        }
    }

    public static boolean isServerRunning() {
        return isServerRunning;
    }

}
