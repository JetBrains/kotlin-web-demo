package web.view.ukhorskaya;

import com.intellij.rt.compiler.JavacRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:35 PM
 */

public class Main {

    public static void main(String[] args) {
        try {
            Initializer.getInstance().initJavaCoreEnvironment();
        } catch (Exception e) {
            System.err.println("FATAL ERROR: Initialisation of java core environment failed, server didn't start");
            e.printStackTrace();
            System.exit(1);
        }

        KotlinHttpServer ideaHttpServer = KotlinHttpServer.getInstance();
        ideaHttpServer.startServer();
    }
}
