package web.view.ukhorskaya;

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import web.view.ukhorskaya.responseHelpers.JsonResponseForHighlighting;
import web.view.ukhorskaya.session.SessionInfo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/28/11
 * Time: 10:46 AM
 */

public class MainApplet extends JApplet implements ActionListener {
    private JButton b1;

    public void init() {
        SessionInfo.IS_ON_SERVER_SESSION = false;
        InitializerApplet.getInstance().initJavaCoreEnvironment();

        /*JFrame frame = new JFrame("ButtonTest");
        frame.setSize(300, 200);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        b1 = new JButton("Button 1");
        b1.addActionListener(this);
        contentPane.add(b1);
        frame.show();*/
    }

    public String getAllExamples() {
        try {
            //ExamplesLoaderApplet loader = new ExamplesLoaderApplet();
            //return loader.getExamplesList();
            return "";
        } catch (Throwable e) {
            StringWriter writer = new StringWriter();

            e.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
//        return null;
//        return "[{\"text\":\"Hello, world!\",\"type\":\"head\"},{\"text\":\"Simplest version.kt\",\"type\":\"content\"},{\"text\":\"Reading a name from the command line.kt\",\"type\":\"content\"},{\"text\":\"Reading many names from the command line.kt\",\"type\":\"content\"},{\"text\":\"A multi-language Hello.kt\",\"type\":\"content\"},{\"text\":\"An object-oriented Hello.kt\",\"type\":\"content\"},{\"text\":\"Basic syntax walk-through\",\"type\":\"head\"},{\"text\":\"Use a conditional expression.kt\",\"type\":\"content\"},{\"text\":\"Null-checks.kt\",\"type\":\"content\"},{\"text\":\"is-checks and automatic casts.kt\",\"type\":\"content\"},{\"text\":\"Use a while-loop.kt\",\"type\":\"content\"},{\"text\":\"Use a for-loop.kt\",\"type\":\"content\"},{\"text\":\"Use ranges and in.kt\",\"type\":\"content\"},{\"text\":\"Use pattern-matching.kt\",\"type\":\"content\"}]";
    }

    public String getHighlighting(String data) {
        try {
            data = ResponseUtils.escapeString(data).replaceAll("\n", " ");
            data = data.replaceAll("\"", "'");
            System.out.println(data);

            JetFile currentPsiFile = JetPsiFactory.createFile(InitializerApplet.getEnvironment().getProject(), data);

            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile);
            String result = responseForHighlighting.getResult();
            System.out.println(result);
            return result;

        } catch (Throwable e) {
            e.printStackTrace();
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson(" my " + writer.toString());
        }
    }

    public String foo() {
        return "[{\"text\":\"Hello, world!\",\"type\":\"head\"},{\"text\":\"Simplest version.kt\",\"type\":\"content\"},{\"text\":\"Reading a name from the command line.kt\",\"type\":\"content\"},{\"text\":\"Reading many names from the command line.kt\",\"type\":\"content\"},{\"text\":\"A multi-language Hello.kt\",\"type\":\"content\"},{\"text\":\"An object-oriented Hello.kt\",\"type\":\"content\"},{\"text\":\"Basic syntax walk-through\",\"type\":\"head\"},{\"text\":\"Use a conditional expression.kt\",\"type\":\"content\"},{\"text\":\"Null-checks.kt\",\"type\":\"content\"},{\"text\":\"is-checks and automatic casts.kt\",\"type\":\"content\"},{\"text\":\"Use a while-loop.kt\",\"type\":\"content\"},{\"text\":\"Use a for-loop.kt\",\"type\":\"content\"},{\"text\":\"Use ranges and in.kt\",\"type\":\"content\"},{\"text\":\"Use pattern-matching.kt\",\"type\":\"content\"}]";
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        getHighlighting("fun main");
    }
}
