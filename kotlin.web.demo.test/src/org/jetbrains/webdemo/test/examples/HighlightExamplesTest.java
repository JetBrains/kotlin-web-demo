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

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Natalia.Ukhorskaya
 */

public class HighlightExamplesTest extends BaseTest {

    public void testExamples() throws IOException {
        File rootDir = new File(ApplicationSettings.EXAMPLES_DIRECTORY);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            return;
        }
        File[] folders = rootDir.listFiles();
        assert folders != null;
        Arrays.sort(folders, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareToIgnoreCase(f2.getName());
//                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        StringBuilder builder = new StringBuilder();
        for (File folder : folders) {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                assert files != null;
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return f1.getName().compareToIgnoreCase(f2.getName());
//                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });
                for (File file : files) {
                    if (file.getName().equals("order.txt")) {
                        continue;
                    }
                    if (!folder.getName().equals("Problems")) {
                        builder.append(file.getName()).append("\n");
                        if (folder.getName().equals("Canvas")) {
                            compareResponseAndExpectedResult(file, "[]", "canvas");
                        } else {
                            compareResponseAndExpectedResult(file, "[]", "java");
                        }
                    }
                }
            }
        }

        StringBuilder expectedList = new StringBuilder();
        expectedList.append("is-checks and smart casts.kt").append("\n");
        expectedList.append("Null-checks.kt").append("\n");
        expectedList.append("Use a conditional expression.kt").append("\n");
        expectedList.append("Use a for-loop.kt").append("\n");
        expectedList.append("Use a while-loop.kt").append("\n");
        expectedList.append("Use ranges and in.kt").append("\n");
        expectedList.append("Use when.kt").append("\n");
        expectedList.append("Creatures.kt").append("\n");
        expectedList.append("Fancy lines.kt").append("\n");
        expectedList.append("Hello, Kotlin.kt").append("\n");
        expectedList.append("Traffic light.kt").append("\n");
        expectedList.append("A multi-language Hello.kt").append("\n");
        expectedList.append("An object-oriented Hello.kt").append("\n");
        expectedList.append("Reading a name from the command line.kt").append("\n");
        expectedList.append("Reading many names from the command line.kt").append("\n");
        expectedList.append("Simplest version.kt").append("\n");
        expectedList.append("99 Bottles of Beer.kt").append("\n");
        expectedList.append("HTML Builder.kt").append("\n");
        expectedList.append("Life.kt").append("\n");
        expectedList.append("Maze.kt").append("\n");
        
        assertEquals("Files to compare", expectedList.toString(), builder.toString());

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
