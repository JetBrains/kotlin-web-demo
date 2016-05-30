/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.examples.Example;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.datastructures.TextPosition;
import org.jetbrains.webdemo.test.BaseTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighlightExamplesTest extends BaseTest {

    private static ArrayList<String> jsExamples = new ArrayList<String>();
    private final Example project;
    private String runConfiguration;


    public HighlightExamplesTest(Example project) {
        super(project.name);
        this.project = project;
        this.runConfiguration = project.confType;
    }

    public HighlightExamplesTest(Example project, String runConfiguration) {
        super(project.name + "_" + runConfiguration);
        this.project = project;
        this.runConfiguration = runConfiguration;
    }

    public static Test suite() {
        jsExamples.add("is-checks and smart casts");
        jsExamples.add("Use a while-loop");
        jsExamples.add("Use a for-loop");
        jsExamples.add("Simplest version");
        jsExamples.add("Reading a name from the command line");
        jsExamples.add("Reading many names from the command line");

        jsExamples.add("A multi-language Hello");
        jsExamples.add("An object-oriented Hello");
        // TODO add test for js with warning
        // jsExamples.add("HTML Builder.kt");


        TestSuite suite = new TestSuite(HighlightExamplesTest.class.getName());
        for (Example project : ExamplesUtils.getAllExamples(ExamplesFolder.ROOT_FOLDER)) {
            suite.addTest(new HighlightExamplesTest(project));
            if (jsExamples.contains(project.name)) {
                suite.addTest(new HighlightExamplesTest(project, "js"));
            }
        }
        return suite;
    }

    @Override
    protected void runTest() throws Throwable {
        Map<String, String> files = new HashMap<>();
        for (ProjectFile file : project.files) {
            files.put(file.getName(), file.getText());
        }
        for (ProjectFile file : project.getHiddenFiles()) {
            files.put(file.getName(), file.getText());
        }

        boolean isJs = project.confType.equals("js") || project.confType.equals("canvas");
        Map<String, List<ErrorDescriptor>> result = kotlinWrapper.getErrors(files, isJs);
        for (String fileName : result.keySet()) {
            assertTrue(getErrorsMessages(fileName, result.get(fileName)), result.get(fileName).isEmpty());
        }

        if (jsExamples.contains(project.name)) {

        }
//        assertEquals("Wrong result for example " + file.getName() + " execute configuration: " + runConfiguration, expectedResult, actualResult);
    }

    private String getErrorsMessages(String fileName, List<ErrorDescriptor> errors) {
        StringBuilder answer = new StringBuilder();
        for (ErrorDescriptor error : errors) {

            TextPosition errorStart = error.getInterval().getStart();
            answer.append('\n')
                    .append(fileName)
                    .append(' ')
                    .append(errorStart.getLine())
                    .append(':')
                    .append(errorStart.getCh())
                    .append(' ')
                    .append(error.getMessage());
        }
        return answer.toString();
    }
}

