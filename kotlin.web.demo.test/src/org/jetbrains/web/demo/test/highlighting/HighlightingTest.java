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

package org.jetbrains.web.demo.test.highlighting;

import junit.framework.TestCase;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.web.demo.test.TestUtils;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.MySqlConnector;
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

public class HighlightingTest extends TestCase {

    private SessionInfo sessionInfo = new SessionInfo("test", SessionInfo.TypeOfRequest.HIGHLIGHT);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
        Initializer.INITIALIZER = ServerInitializer.getInstance();

        ServerSettings.JAVA_HOME = "c:\\\\Program Files\\\\Java\\\\jdk1.6.0_30\\";

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

    public void test$errors$oneError() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "[{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\"," +
                "\"className\":\"red_wavy_line\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 13}\"," +
                "\"x\":\"{line: 1, ch: 12}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
        expectedResult = "[{\"titleName\":\"Unresolved reference: System\"," +
                "\"className\":\"ERROR\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 8}\"," +
                "\"x\":\"{line: 1, ch: 2}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "js");
    }

    public void test$warnings$oneWarning() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "[{\"titleName\":\"Unnecessary safe call on a non-null receiver of type Int\"," +
                "\"className\":\"WARNING\"," +
                "\"severity\":\"WARNING\"," +
                "\"y\":\"{line: 2, ch: 7}\"," +
                "\"x\":\"{line: 2, ch: 5}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    public void test$errors$twoErrorsInOneLine() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "[{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\"," +
                "\"className\":\"red_wavy_line\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 15}\"," +
                "\"x\":\"{line: 1, ch: 14}\"}," +
                "{\"titleName\":\"Expecting ')'\"," +
                "\"className\":\"red_wavy_line\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 30}\"," +
                "\"x\":\"{line: 1, ch: 29}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    private void compareResponseAndExpectedResult(String fileName, String expectedResult, String runConfiguration) throws IOException {
        sessionInfo.setRunConfiguration(runConfiguration);
        JetFile currentPsiFile = JetPsiFactory.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));

        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, sessionInfo);
        String actualResult = responseForHighlighting.getResult();

        assertEquals("Wrong result", expectedResult, actualResult);
    }

}
