package web.view.ukhorskaya;

import com.sun.net.httpserver.HttpServer;
import web.view.ukhorskaya.handlers.BaseHandler;

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
    //public static Class<? extends BaseHandler> ourHandlerClass = MainHandler.class;

    public static KotlinHttpServer getInstance() {
        return myServer;
    }

    private BaseHandler myHandler;

    private KotlinHttpServer() {

    }

    public void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(80), 10);
        } catch (Exception e) {
            System.err.println("FATAL ERROR: Server didn't start");
            e.printStackTrace();
            System.exit(1);
        }
        if (myHandler == null) {
            myHandler = new BaseHandler();
            //myHandler = ourHandlerClass.newInstance();
        }
        server.createContext("/", myHandler);
        server.setExecutor(null);
        server.start();

        System.out.println("Server is started");
    }

    public static void stopServer() {
        KotlinHttpServer.server.stop(0);
        System.out.println("Server is stopped");
    }

    public static boolean isServerRunning() {
        return server != null;
    }

}
