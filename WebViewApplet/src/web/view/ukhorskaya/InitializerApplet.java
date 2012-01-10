package web.view.ukhorskaya;

import com.intellij.openapi.Disposable;
import org.jetbrains.jet.JetCoreEnvironment;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.plugin.JetFileType;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:49 PM
 */
public class InitializerApplet {
    private static InitializerApplet initializer = new InitializerApplet();

    public static InitializerApplet getInstance() {
        return initializer;
    }

    private InitializerApplet() {
    }

    private static JetCoreEnvironment environment;

    public static JetCoreEnvironment getEnvironment() {
        if (environment != null) {
            return environment;
        }
        return null;
    }

    public boolean initJavaCoreEnvironment() {
        if (environment == null) {

            Disposable root = new Disposable() {
                @Override
                public void dispose() {
                }
            };
            environment = new JetCoreEnvironment(root);

//            environment.addToClasspath(new File(InitializerApplet.class.getResource("rt.jar")));
//            environment.addToClasspath(new File("kotlin-runtime.jar"));
//            environment.addToClasspath(new File("kotlin-compiler.jar"));

            environment.registerFileType(JetFileType.INSTANCE, "kt");
            environment.registerFileType(JetFileType.INSTANCE, "kts");
            environment.registerFileType(JetFileType.INSTANCE, "ktm");
            environment.registerFileType(JetFileType.INSTANCE, "jet");
            environment.registerParserDefinition(new JetParserDefinition());


            return true;
            //return setJavaCoreEnvironment();
        }
        return false;
    }
}


