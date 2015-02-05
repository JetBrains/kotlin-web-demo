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

package org.jetbrains.webdemo.test.completion;

import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.webdemo.JetPsiFactoryUtil;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForCompletion;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;
import org.json.JSONException;

import java.io.IOException;

public class CompletionTest extends BaseTest {

    public void test$java$system() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"class\",\"name\":\"System\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"SystemClassLoaderAction\",\"tail\":\" (java.lang)\"}]";
        compareResult(1, 6, expectedResult, "java");
    }

    public void test$java$out() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"property\",\"name\":\"out\",\"tail\":\"PrintStream?\"}]";
        compareResult(1, 10, expectedResult, "java");
    }

    public void test$all$variable() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"property\",\"name\":\"stdin\",\"tail\":\"BufferedReader\"},{\"icon\":\"method\",\"name\":\"stream(() -> T?)\",\"tail\":\"Stream<T>\"},{\"icon\":\"method\",\"name\":\"stream(T, (T) -> T?)\",\"tail\":\"Stream<T>\"},{\"icon\":\"method\",\"name\":\"streamOf(Progression<T>)\",\"tail\":\"Stream<T>\"},{\"icon\":\"method\",\"name\":\"streamOf(vararg T)\",\"tail\":\"Stream<T>\"},{\"icon\":\"property\",\"name\":\"str\",\"tail\":\"String\"}]";
        compareResult(14, 24, expectedResult, "java");
        compareResult(14, 24, expectedResult, "js");
    }

    public void test$all$class() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"class\",\"name\":\"Greeter\",\"tail\":\" (<root>)\"}]";
        compareResult(4, 3, expectedResult, "java");
        compareResult(4, 3, expectedResult, "js");
    }

    public void test$all$type$in$constructor() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"class\",\"name\":\"StrictMath\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"String\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"String\",\"tail\":\" (kotlin)\"},{\"icon\":\"class\",\"name\":\"StringBuffer\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"StringBuilder\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"StringCoding\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"StringIndexOutOfBoundsException\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"name\":\"StringValue\",\"tail\":\" (java.lang)\"}]";
        compareResult(12, 28, expectedResult, "java");
        compareResult(12, 28, expectedResult, "js");
    }

    public void test$java$println() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"method\",\"name\":\"print(Any?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(Char)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(CharArray?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(Double)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(Float)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(Int)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(Long)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"print(String?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"printf(Locale?, String, vararg Any?)\",\"tail\":\"PrintStream\"},{\"icon\":\"method\",\"name\":\"printf(String, vararg Any?)\",\"tail\":\"PrintStream\"},{\"icon\":\"method\",\"name\":\"println()\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Any?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Char)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(CharArray?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Double)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Float)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Int)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(Long)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"println(String?)\",\"tail\":\"Unit\"}]";

        compareResult(1, 15, expectedResult, "java");
    }

    public void test$js$a() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"genericValue\",\"name\":\"args\",\"tail\":\"Array<String>\"},{\"icon\":\"method\",\"name\":\"array(vararg T)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"name\":\"arrayList(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"name\":\"arrayListOf(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"name\":\"arrayOfNulls(Int)\",\"tail\":\"Array<T?>\"},{\"icon\":\"method\",\"name\":\"assert(Boolean, () -> Any)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"assert(Boolean, Any)\",\"tail\":\"Unit\"}]";

        compareResult(2, 3, expectedResult, "js");
    }

    public void test$kt$a() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"genericValue\",\"name\":\"args\",\"tail\":\"Array<String>\"},{\"icon\":\"method\",\"name\":\"array(vararg T)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"name\":\"arrayList(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"name\":\"arrayListOf(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"name\":\"arrayOfNulls(Int)\",\"tail\":\"Array<T?>\"},{\"icon\":\"method\",\"name\":\"assert(Boolean, () -> Any)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"name\":\"assert(Boolean, Any)\",\"tail\":\"Unit\"}]";
        compareResult(2, 3, expectedResult, "java");
    }

    private void compareResult(int lineNumber, int charNumber, String expectedResult, String runConfiguration) throws IOException, JSONException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
        sessionInfo.setRunConfiguration(runConfiguration);
        JetFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, "completion/root.kt"));

        JsonResponseForCompletion responseForCompletion = new JsonResponseForCompletion(lineNumber, charNumber, currentPsiFile, sessionInfo);
        String actualResult = responseForCompletion.getResult();

        assertEquals("Wrong result: " + lineNumber + ", " + charNumber, expectedResult, actualResult);
    }
}
