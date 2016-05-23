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

import org.jetbrains.webdemo.test.BaseTest;

public class RunExamplesTest extends BaseTest {

//    private static ArrayList<String> jsExamples = new ArrayList<String>();
//    private final Example project;
//    private final String runConfiguration;
//
//    public RunExamplesTest(Example project) {
//        super(project.name);
//        this.project = project;
//        this.runConfiguration = project.confType;
//    }
//
//    public RunExamplesTest(Example project, String runConfiguration) {
//        super(project.name + "_" + runConfiguration);
//        this.project = project;
//        this.runConfiguration = runConfiguration;
//    }
//
//    public static Test suite() {
//        jsExamples.add("is-checks and smart casts");
//        jsExamples.add("Use a while-loop");
//        jsExamples.add("Use a for-loop");
//        jsExamples.add("Simplest version");
//        jsExamples.add("Reading a name from the command line");
//        jsExamples.add("Reading many names from the command line");
//        jsExamples.add("A multi-language Hello");
//        jsExamples.add("An object-oriented Hello");
//        //jsExamples.add("HTML Builder.kt");
//
//        TestSuite suite = new TestSuite(RunExamplesTest.class.getName());
//        for (Example project : ExamplesUtils.getAllExamples(ExamplesFolder.ROOT_FOLDER)) {
//            suite.addTest(new RunExamplesTest(project));
//            if (jsExamples.contains(project.name)) {
//                suite.addTest(new RunExamplesTest(project, "js"));
//            }
//        }
//        return suite;
//    }
//
//
//    @Override
//    protected void runTest() throws Throwable {
//        BackendSessionInfo sessionInfo = new BackendSessionInfo("test", BackendSessionInfo.TypeOfRequest.RUN);
//        sessionInfo.setRunConfiguration(runConfiguration);
//        List<PsiFile> psiFiles = new ArrayList<>();
//        for (ProjectFile file : project.files) {
//            psiFiles.add(JetPsiFactoryUtil.createFile(getProject(), file.getName(), file.getText()));
//        }
//        for (ProjectFile file : project.getHiddenFiles()){
//            psiFiles.add(JetPsiFactoryUtil.createFile(getProject(), file.getName(), file.getText()));
//        }
//
//        if (sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JAVA)) {
//            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(psiFiles, getProject(), sessionInfo, project.args);
//            ArrayNode actualResult = (ArrayNode) new ObjectMapper().readTree(responseForCompilation.getResult());
//            boolean isAnyOutput = false;
//            for (JsonNode outputObject : actualResult) {
//                if (outputObject.get("type").asText().equals("out")) {
//                    isAnyOutput = true;
//                    if (project.expectedOutput != null) {
//                        assertEquals(unifyLineSeparators(project.expectedOutput), getStdOut(outputObject.get("text").asText()));
//                    }
//                    assertTrue(outputObject.get("exception").isNull());
//                }
//            }
//            assertTrue("No program output", isAnyOutput);
//        } else if (sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JUNIT)) {
//            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(psiFiles, getProject(), sessionInfo, project.args);
//            ArrayNode actualResult = (ArrayNode) new ObjectMapper().readTree(responseForCompilation.getResult());
//            boolean isAnyOutput = false;
//            for (JsonNode outputObject : actualResult){
//                if(outputObject.get("type").asText().equals("out")) {
//                    isAnyOutput = true;
//                    ArrayNode testResults = (ArrayNode) outputObject.get("testResults");
//                    assertTrue("No test results", testResults.size() > 0);
//                    for (JsonNode testResult : testResults) {
//                        String message = testResult.get("className").asText() + "." + testResult.get("methodName").asText() + " status:" + testResult.get("status").asText();
//                        assertEquals(message, "OK", testResult.get("status").asText());
//                    }
//                }
//            }
//            assertTrue("No program output", isAnyOutput);
//        }
//    }
//
//    private String getStdOut(String programOutput) {
//        programOutput = Common.unEscapeString(programOutput).replaceAll("\\r\\n", "</br>").replaceAll("\\n", "</br>");
//        StringBuilder builder = new StringBuilder();
//        Matcher matcher = Pattern.compile("<outStream>(.*)</outStream>").matcher(programOutput);
//        while (matcher.find()) {
//            builder.append(matcher.group(1));
//        }
//        return unifyLineSeparators(builder.toString());
//    }
//
//    private String unifyLineSeparators(String text) {
//        return text.replaceAll("\\r\\n", "</br>").replaceAll("\\n", "</br>").replaceAll("</br>", System.lineSeparator());
//    }
}

