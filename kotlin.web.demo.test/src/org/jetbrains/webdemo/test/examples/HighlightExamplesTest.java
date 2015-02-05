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

package org.jetbrains.webdemo.test.examples;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.webdemo.JetPsiFactoryUtil;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HighlightExamplesTest extends BaseTest {

    private static ArrayList<String> jsExamples = new ArrayList<String>();

    public static Test suite() {
        jsExamples.add("is-checks and smart casts.kt");
        jsExamples.add("Use a while-loop.kt");
        jsExamples.add("Use a for-loop.kt");
        jsExamples.add("Simplest version.kt");
        jsExamples.add("Reading a name from the command line.kt");
        jsExamples.add("Reading many names from the command line.kt");

        jsExamples.add("A multi-language Hello.kt");
        jsExamples.add("An object-oriented Hello.kt");
        // TODO add test for js with warning
        // jsExamples.add("HTML Builder.kt");


        TestSuite suite = new TestSuite(HighlightExamplesTest.class.getName());
        File parsingSourceDir = new File(ApplicationSettings.EXAMPLES_DIRECTORY);
        addFilesFromDirToSuite(parsingSourceDir, suite);
        return suite;
    }

    private static void addFilesFromDirToSuite(File file, TestSuite ats) {
        if (file.isDirectory()) {
            for (File sourceFile : file.listFiles()) {
                if (!file.getName().equals("Problems")) {
                    addFilesFromDirToSuite(sourceFile, ats);
                }
            }
        }
        else {
            if (file.getName().equals("order.txt")) {
                return;
            }
            if (file.getName().endsWith(".kt")) {
                if (file.getParentFile().getName().equals("Canvas")) {
                    ats.addTest(new HighlightExamplesTest(file, "canvas"));
                }
                else {
                    if (jsExamples.contains(file.getName())) {
                        ats.addTest(new HighlightExamplesTest(file, "js"));
                    }
                    ats.addTest(new HighlightExamplesTest(file, "java"));
                }
            }
        }
    }


    private final File sourceFile;
    private final String runConf;

    public HighlightExamplesTest(File sourceFile, String runConf) {
        super(sourceFile.getName());
        this.sourceFile = sourceFile;
        this.runConf = runConf;
    }

    @Override
    protected void runTest() throws Throwable {
        compareResponseAndExpectedResult(sourceFile, runConf);
    }

    private void compareResponseAndExpectedResult(File file, String runConfiguration) throws IOException {
        String expectedResult = "[]";
        sessionInfo.setRunConfiguration(runConfiguration);
        JetFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), TestUtils.getDataFromFile(file));

        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, sessionInfo);
        String actualResult = responseForHighlighting.getResult();

        assertEquals("Wrong result for example " + file.getName() + " run configuration: " + runConfiguration, expectedResult, actualResult);
    }
}

