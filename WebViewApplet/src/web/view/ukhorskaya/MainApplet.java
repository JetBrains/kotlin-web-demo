package web.view.ukhorskaya;

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import web.view.ukhorskaya.responseHelpers.JsonResponseForCompletion;
import web.view.ukhorskaya.responseHelpers.JsonResponseForHighlighting;
import web.view.ukhorskaya.session.SessionInfo;

import javax.swing.*;
import java.awt.*;
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
    private JButton b2;

    public static String request;

    public void init() {
        InitializerApplet.getInstance().initJavaCoreEnvironment();
        request = getCodeBase().getProtocol() + "://" + getCodeBase().getHost();
        ErrorsWriter.errorsWriter = ErrorsWriterInApplet.getInstance();

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout());
        b1 = new JButton("highlighting");
        b1.addActionListener(this);
        contentPane.add(b1);
        b2 = new JButton("completion");
        b2.addActionListener(this);
        contentPane.add(b2);
    }

    public String getHighlighting(String data) {
        SessionInfo.TYPE = SessionInfo.TypeOfRequest.HIGHLIGHT;
        try {
            JetFile currentPsiFile = JetPsiFactory.createFile(InitializerApplet.getEnvironment().getProject(), data);
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile);
            String result = responseForHighlighting.getResult();
            System.out.println(result);
            return result;

        } catch (Throwable e) {
            ErrorsWriter.errorsWriter.writeException(ErrorsWriter.getExceptionForLog(SessionInfo.TYPE.name(), e, data));
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson("MainApplet.java " + writer.toString());
        }
    }

    public String getCompletion(String data, String line, String ch) {
        SessionInfo.TYPE = SessionInfo.TypeOfRequest.COMPLETE;
        try {
            JetFile currentPsiFile = JetPsiFactory.createFile(InitializerApplet.getEnvironment().getProject(), data);
            System.out.println(Integer.parseInt(line) + " " + Integer.parseInt(ch) + " " + currentPsiFile.getText());
            JsonResponseForCompletion responseForCompletion = new JsonResponseForCompletion(Integer.parseInt(line), Integer.parseInt(ch), currentPsiFile);
            String result = responseForCompletion.getResult();
            System.out.println(result);
            return result;

        } catch (Throwable e) {
            ErrorsWriter.errorsWriter.writeException(ErrorsWriter.getExceptionForLog(SessionInfo.TYPE.name(), e, data));
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson("MainApplet.java " + writer.toString());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new Object();
        if (e.getActionCommand().equals("highlighting")) {
            getHighlighting("fun main() { val a = java.util.ArrayList<String>(); System.out?.println(\"sss\" + a}");
        } else if (e.getActionCommand().equals("completion")) {
            getCompletion("fun main() { System.out?.println(\"sss\" + a)}", "0", "20");
        }
//        getHighlighting("fun main() { val a = Object()}");
//        getHighlighting("fun main() { val a = String(\"aaa\") }");
    }
}
