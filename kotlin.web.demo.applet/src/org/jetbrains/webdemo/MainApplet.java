/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForCompletion;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.translator.WebDemoConfigApplet;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

public class MainApplet extends JApplet implements ActionListener {
    /*private JButton b1;*/

    public static String request;

    public static SessionInfo SESSION_INFO;

    public void init() {
        try {
            InitializerApplet.getInstance().initJavaCoreEnvironment();
            request = getCodeBase().getProtocol() + "://" + getCodeBase().getHost();
            ErrorWriter.ERROR_WRITER = ErrorWriterInApplet.getInstance();
            Initializer.INITIALIZER = InitializerApplet.getInstance();
            WebDemoTranslatorFacade.LOAD_JS_LIBRARY_CONFIG = new WebDemoConfigApplet(Initializer.INITIALIZER.getEnvironment().getProject());

            SESSION_INFO = new SessionInfo("applet" + new Random().nextInt());
            getHighlighting("fun main(args : Array<String>) {\n" +
                    "  System.out?.println(\"Hello, world!\"\n" +
                    "}");

        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Cannot start applet");
        }
    }

    public String getHighlighting(String data) {
        return getHighlighting(data, "java");
    }

    public String getHighlighting(String data, String runConfiguration) {
        System.out.println("get_highlighting");
        SESSION_INFO.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            JetFile currentPsiFile = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), data);
            SESSION_INFO.setRunConfiguration(runConfiguration);

            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, SESSION_INFO);
            return responseForHighlighting.getResult();

        } catch (Throwable e) {
            e.printStackTrace();
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SESSION_INFO.getType(), data);
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson(writer.toString());
        }
    }

    @Nullable
    public String translateToJS(@NotNull String code, @NotNull String arguments) {
        System.out.println("translate");
        try {
            return WebDemoTranslatorFacade.translateStringWithCallToMain(code, arguments);
        } catch (Throwable e) {
            e.printStackTrace();
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), code);
            return ResponseUtils.getErrorInJson("Unexpected exception.");
        }
    }


    public String getCompletion(String data, String line, String ch, String runConfiguration) {
        System.out.println("get_completion");
        SESSION_INFO.setType(SessionInfo.TypeOfRequest.COMPLETE);
        try {
            JetFile currentPsiFile = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), data);
            SESSION_INFO.setRunConfiguration(runConfiguration);

            JsonResponseForCompletion responseForCompletion;
            try {
                responseForCompletion = new JsonResponseForCompletion(Integer.parseInt(line),
                        Integer.parseInt(ch), currentPsiFile, SESSION_INFO);
            } catch (NumberFormatException e) {
                responseForCompletion = new JsonResponseForCompletion((int) Double.parseDouble(line),
                        (int) Double.parseDouble(ch), currentPsiFile, SESSION_INFO);
            }
            return responseForCompletion.getResult();

        } catch (Throwable e) {
            e.printStackTrace();
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SESSION_INFO.getType(), data + " line: " + line + " ch: " + ch);
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return ResponseUtils.getErrorInJson(writer.toString());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        getHighlighting("fun main() { val a = String(\"aaa\") }");
    }
}
