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

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.web.demo.test.BaseTest;
import org.jetbrains.web.demo.test.TestUtils;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.responseHelpers.CompileAndRunExecutor;
import org.jetbrains.webdemo.responseHelpers.JsConverter;
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Natalia.Ukhorskaya
 */

public class RunExamplesTest extends BaseTest {

    private Map<String, Example> expectedResults = new HashMap<String, Example>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        expectedResults.put("Use a conditional expression.kt", new Example("Use a conditional expression.kt", "10 20", "20<br/>"));
        expectedResults.put("Null-checks.kt", new Example("Null-checks.kt", "2 3", "6<br/>"));
        expectedResults.put("is-checks and smart casts.kt", new Example("is-checks and smart casts.kt", "", "3<br/>null<br/>"));
        expectedResults.put("Use a while-loop.kt", new Example("Use a while-loop.kt", "guest1 guest2 guest3 guest4", "guest1<br/>guest2<br/>guest3<br/>guest4<br/>"));
        expectedResults.put("Use a for-loop.kt", new Example("Use a for-loop.kt", "guest1 guest2 guest3", "guest1<br/>guest2<br/>guest3<br/><br/>guest1<br/>guest2<br/>guest3<br/>"));
        expectedResults.put("Use ranges and in.kt", new Example("Use ranges and in.kt", "4", "OK<br/>1 2 3 4 5 <br/>Out: array has only 3 elements. x = 4<br/>Yes: array contains aaa<br/>No: array doesn't contains ddd<br/>"));
        expectedResults.put("Use when.kt", new Example("Use when.kt", "", "Greeting<br/>One<br/>Long<br/>Not a string<br/>Unknown<br/>"));
        expectedResults.put("Creatures.kt", new Example("Creatures.kt", "", "from js file"));
        expectedResults.put("Fancy lines.kt", new Example("Fancy lines.kt", "", "from js file"));
        expectedResults.put("Hello, Kotlin.kt", new Example("Hello, Kotlin.kt", "", "from js file"));
        expectedResults.put("A multi-language Hello.kt", new Example("A multi-language Hello.kt", "FR", "Salut!<br/>"));
        expectedResults.put("An object-oriented Hello.kt", new Example("An object-oriented Hello.kt", "guest1", "Hello, guest1<br/>"));
        expectedResults.put("Reading a name from the command line.kt", new Example("Reading a name from the command line.kt", "guest1", "Hello, guest1!<br/>"));
        expectedResults.put("Reading many names from the command line.kt", new Example("Reading many names from the command line.kt", "guest1 guest2 guest3", "Hello, guest1!<br/>Hello, guest2!<br/>Hello, guest3!<br/>"));
        expectedResults.put("Simplest version.kt", new Example("Simplest version.kt", "", "Hello, world!<br/>"));
        expectedResults.put("99 Bottles of Beer.kt", new Example("99 Bottles of Beer.kt", "", "from txt file"));
        expectedResults.put("HTML Builder.kt", new Example("HTML Builder.kt", "", "from txt file"));
        expectedResults.put("Maze.kt", new Example("Maze.kt", "", "from txt file"));
        expectedResults.put("Life.kt", new Example("Life.kt", "", "from txt file"));
        expectedResults.put("", new Example("", "", ""));

    }

    public void testExamples() throws IOException, JSONException {
        File rootDir = new File(ServerSettings.EXAMPLES_ROOT);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            return;
        }
        File[] folders = rootDir.listFiles();
        assert folders != null;
        StringBuilder builder = new StringBuilder();
        for (File folder : folders) {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                assert files != null;
                for (File file : files) {
                    if (file.getName().equals("order.txt")) {
                        continue;
                    }
                    if (!folder.getName().equals("Problems")) {
                        builder.append(file.getName()).append("\n");
                        if (folder.getName().equals("Canvas")) {
                            compareResponseAndExpectedResult(file, "canvas");
                        } else {
                            compareResponseAndExpectedResult(file, "java");
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

    private void compareResponseAndExpectedResult(File file, String runConfiguration) throws IOException, JSONException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        sessionInfo.setRunConfiguration(runConfiguration);

        Example example = getExampleInfo(file.getName());

        assertEquals("Cannot find info for example " + file.getName(), true, example != null);

        StringBuilder output = new StringBuilder();
        String actualResult;
        if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
            JetFile currentPsiFile = JetPsiFactory.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), TestUtils.getDataFromFile(file));
            sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);

            assert example != null;
            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(currentPsiFile, example.getArgs(), sessionInfo);
            actualResult = responseForCompilation.getResult();

            JSONArray actualArray = new JSONArray(actualResult);


            for (int i = 0; i < actualArray.length(); i++) {
                JSONObject object = (JSONObject) actualArray.get(i);
                if (object.get("type").equals("out")) {
                    output.append(object.get("text"));
                }
            }
        } else {
            sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
            assert example != null;
            actualResult = new JsConverter(sessionInfo).getResult(TestUtils.getDataFromFile(file), example.getArgs());
            JSONArray actualArray = new JSONArray(actualResult);

            JSONObject object = (JSONObject) actualArray.get(0);
            output.append(object.get("text"));
        }


        assertEquals("Wrong result for " + file.getName(), example.getResult(), output.toString());
    }

    private Example getExampleInfo(String name) {
        return expectedResults.get(name);
    }

    private class Example {
        private String fileName;
        private String result;
        private String args;

        private Example(String fileName, String args, String result) {
            this.fileName = fileName;
            this.result = result;
            this.args = args;
        }

        public String getResult() throws IOException {
            if (result.equals("from js file")) {
                return TestUtils.getDataFromFile(TestUtils.TEST_SRC, "execution" + File.separator + "jsExamples" + File.separator + fileName.replace(".kt", ".js"));
            } else if (result.equals("from txt file")) {
                String resultFromFile = TestUtils.getDataFromFile(TestUtils.TEST_SRC, "execution" + File.separator + "txtExamples" + File.separator + fileName.replace(".kt", ".txt"));
                if (fileName.equals("HTML Builder.kt")) {
                    //TODO Find where appears br at the end
                    resultFromFile = escape(resultFromFile) + "<br/>";
                } else if (fileName.equals("Life.kt")) {
                    resultFromFile = resultFromFile.replaceAll("([_])", " ");
                } else if (fileName.equals("Maze.kt")) {
                    resultFromFile = resultFromFile.replaceAll("([_])", " ") + "<br/>";
                }
                resultFromFile = resultFromFile.replaceAll("([\n])", "<br/>");
                return resultFromFile;
            } else {
                return result;
            }

        }

        public String getArgs() {
            return args;
        }
    }

    private String escape(String str) {
        str = str.replaceAll("<", "&amp;lt;");
        str = str.replaceAll(">", "&amp;gt;");
        return str;
    }
}
