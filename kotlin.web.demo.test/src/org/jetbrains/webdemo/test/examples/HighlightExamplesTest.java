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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.PsiFile;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.JetPsiFactoryUtil;
import org.jetbrains.webdemo.backend.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.test.BaseTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HighlightExamplesTest extends BaseTest {

    private static ArrayList<String> jsExamples = new ArrayList<String>();
    private final Project project;
    private String runConfiguration;


    public HighlightExamplesTest(Project project) {
        super(project.name);
        this.project = project;
        this.runConfiguration = project.confType;
    }

    public HighlightExamplesTest(Project project, String runConfiguration) {
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
        for (Project project : ExamplesUtils.getAllExamples()) {
            if (!project.parent.equals("Problems")) {
                suite.addTest(new HighlightExamplesTest(project));
            }
            if (jsExamples.contains(project.name)) {
                suite.addTest(new HighlightExamplesTest(project, "js"));
            }
        }
        return suite;
    }

    @Override
    protected void runTest() throws Throwable {
        BackendSessionInfo sessionInfo = new BackendSessionInfo("test");
        sessionInfo.setRunConfiguration(runConfiguration);
        List<PsiFile> psiFiles = new ArrayList<>();
        for (ProjectFile file : project.files) {
            psiFiles.add(JetPsiFactoryUtil.createFile(getProject(), file.getName(), file.getText()));
        }

        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(psiFiles, sessionInfo, getProject());
        ObjectNode actualResult = (ObjectNode) new ObjectMapper().readTree(responseForHighlighting.getResult());

        Iterator<JsonNode> fields = actualResult.elements();
        while (fields.hasNext()) {
            ArrayNode errors = (ArrayNode) fields.next();
            assertEquals(errors.size(), 0);
        }

        if (jsExamples.contains(project.name)) {

        }
//        assertEquals("Wrong result for example " + file.getName() + " run configuration: " + runConfiguration, expectedResult, actualResult);
    }
}

