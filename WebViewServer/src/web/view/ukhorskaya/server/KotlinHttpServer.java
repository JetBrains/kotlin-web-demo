package web.view.ukhorskaya.server;

import com.sun.net.httpserver.HttpServer;
import web.view.ukhorskaya.ErrorWriter;
import web.view.ukhorskaya.handlers.ServerHandler;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

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
                    server = HttpServer.create(new InetSocketAddress(Integer.parseInt(ServerSettings.PORT)), 20);
                } else {
                    server = HttpServer.create(new InetSocketAddress(ServerSettings.HOST, Integer.parseInt(ServerSettings.PORT)), 10);
                }
                if (myHandler == null) {
                    myHandler = new ServerHandler();
                }
                server.createContext("/", myHandler);

                /*corePoolSize - the number of threads to keep in the pool, even if they are idle.
                maximumPoolSize - the maximum number of threads to allow in the pool.
                keepAliveTime - when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
                unit - the time unit for the keepAliveTime argument.
                workQueue - the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute method.
                */

                server.setExecutor(Executors.newFixedThreadPool(Integer.parseInt(ServerSettings.MAX_THREAD_COUNT)));
//                server.setExecutor(null);
                server.start();
                isServerRunning = true;
                ErrorWriter.writeInfoToConsole("Server is started at " + KotlinHttpServer.getHost() + ":" + KotlinHttpServer.getPort());
            } else {
                ErrorWriter.writeErrorToConsole("Server is already running at " + KotlinHttpServer.getHost() + ":" + KotlinHttpServer.getPort()
                        + ". Use \"stop\" or \"restart\" commands.");
            }
        } catch (BindException e) {
            ErrorWriter.writeExceptionToConsole("Address already in use. Use \"set hostname\" command to change it.", e);
        } catch (Exception e) {
            ErrorWriter.writeExceptionToConsole("Server didn't start. Use \"start\" to try start server again.", e);
        }
    }

    public static void stopServer() {
        if (isServerRunning) {
            KotlinHttpServer.server.stop(0);
            ErrorWriter.writeInfoToConsole("Server is stopped at " + KotlinHttpServer.getHost() + ":" + KotlinHttpServer.getPort());
            isServerRunning = false;
        } else {
            ErrorWriter.writeErrorToConsole("Server is not running");
        }
    }

    public static boolean isServerRunning() {
        return isServerRunning;
    }

}
