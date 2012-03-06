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

package org.jetbrains.web.demo.test.examples;

import junit.framework.TestCase;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.web.demo.test.TestUtils;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;
import java.io.IOException;

/**
 * @author Natalia.Ukhorskaya
 */

public class ExamplesTest extends TestCase {

    private SessionInfo sessionInfo = new SessionInfo("test", SessionInfo.TypeOfRequest.HIGHLIGHT);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
        Initializer.INITIALIZER = ServerInitializer.getInstance();

        ServerSettings.JAVA_HOME = "c:\\\\Program Files\\\\Java\\\\jdk1.6.0_30\\";
        ServerSettings.EXAMPLES_ROOT = "c:\\\\Development\\\\kotlin-web-demo\\\\examples\\\\";

        new File(ServerSettings.LOGS_ROOT).mkdir();
        new File(ServerSettings.STATISTICS_ROOT).mkdir();

        try {
            if (ServerInitializer.getInstance().initJavaCoreEnvironment()) {
                ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                ExamplesList.getInstance();
                HelpLoader.getInstance();
                Statistics.getInstance();
            } else {
                ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
            }
        } catch (Exception e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }
    }

    public void testExamples() throws IOException {
        File rootDir = new File(ServerSettings.EXAMPLES_ROOT);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            return;
        }
        File[] folders = rootDir.listFiles();
        assert folders != null;
        for (File folder : folders) {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                assert files != null;
                for (File file : files) {
                    if (file.getName().equals("order.txt")) {
                        continue;
                    }
                    if (folder.getName().equals("Problems")) {
                        continue;
                    } else if (folder.getName().equals("Canvas")) {
                        compareResponseAndExpectedResult(file, "[]", "canvas");
                    } else {
                        compareResponseAndExpectedResult(file, "[]", "java");
                    }
                }
            }
        }

    }

    private String modifyExampleName(String name) {
        if (name.contains("hello")) {
            name.replace("hello", "Hello, world!");
        } else if (name.contains("basic")) {
            name.replace("basic", "Basic syntax walk-through");
        } else if (name.contains("longer")) {
            name.replace("longer", "Longer examples");
        }
        return name;
    }

    private void compareResponseAndExpectedResult(File file, String expectedResult, String runConfiguration) throws IOException {
        sessionInfo.setRunConfiguration(runConfiguration);
        JetFile currentPsiFile = JetPsiFactory.createFile(
                Initializer.INITIALIZER.getEnvironment().getProject(),
                TestUtils.getDataFromFile(file));

        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, sessionInfo);
        String actualResult = responseForHighlighting.getResult();

        assertEquals("Wrong result for example " + file.getName(), expectedResult, actualResult);
    }
}
