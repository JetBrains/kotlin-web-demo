package web.view.ukhorskaya;

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import web.view.ukhorskaya.responseHelpers.JsonResponseForCompletion;
import web.view.ukhorskaya.responseHelpers.JsonResponseForHighlighting;
import web.view.ukhorskaya.session.SessionInfo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/28/11
 * Time: 10:46 AM
 */

public class MainApplet extends JApplet implements ActionListener {
    /*private JButton b1;
    private JButton b2;*/

    public static String request;
    
    public static SessionInfo SESSION_INFO;

    public void init() {
        InitializerApplet.getInstance().initJavaCoreEnvironment();
        request = getCodeBase().getProtocol() + "://" + getCodeBase().getHost();
        ErrorWriter.ERROR_WRITER = ErrorWriterInApplet.getInstance();
        
        SESSION_INFO = new SessionInfo(new Random().nextInt(), "unknown");

        /*Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout());
        b1 = new JButton("highlighting");
        b1.addActionListener(this);
        contentPane.add(b1);
        b2 = new JButton("completion");
        b2.addActionListener(this);
        contentPane.add(b2);*/
    }

    public String getHighlighting(String data) {
        SESSION_INFO.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            JetFile currentPsiFile = JetPsiFactory.createFile(InitializerApplet.getEnvironment().getProject(), data);
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, SESSION_INFO);
            return responseForHighlighting.getResult();

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeException(ErrorWriter.getExceptionForLog(SESSION_INFO.getType(), e, data));
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson(writer.toString());
        }
    }

    public String getCompletion(String data, String line, String ch) {
        SESSION_INFO.setType(SessionInfo.TypeOfRequest.COMPLETE);
        try {
            JetFile currentPsiFile = JetPsiFactory.createFile(InitializerApplet.getEnvironment().getProject(), data);
            JsonResponseForCompletion responseForCompletion = new JsonResponseForCompletion(Integer.parseInt(line),
                    Integer.parseInt(ch), currentPsiFile, SESSION_INFO);
            return responseForCompletion.getResult();

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeException(ErrorWriter.getExceptionForLog(SESSION_INFO.getType(), e, data));
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson(writer.toString());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /*if (e.getActionCommand().equals("highlighting")) {
            getHighlighting("fun main() { val a = java.util.ArrayList<String>(); System.out?.println(\"sss\" + a}");
        } else if (e.getActionCommand().equals("completion")) {
            getCompletion("import fun main() { System.out?.println(\"sss\" + a)}", "0", "7");
        }*/
//        getHighlighting("fun main() { val a = Object()}");
//        getHighlighting("fun main() { val a = String(\"aaa\") }");
    }
}
