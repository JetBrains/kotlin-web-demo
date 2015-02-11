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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.backend.JetPsiFactoryUtil;
import org.jetbrains.webdemo.backend.responseHelpers.JsonResponseForCompletion;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CompletionTest extends BaseTest {

    public void test$java$system() throws IOException {
        String expectedResult = "[{\"icon\":\"class\",\"text\":\"System\",\"displayText\":\"System\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"SystemClassLoaderAction\",\"displayText\":\"SystemClassLoaderAction\",\"tail\":\" (java.lang)\"}]";
        compareResult(1, 6, expectedResult, "java");
    }

    public void test$java$out() throws IOException {
        String expectedResult = "[{\"icon\":\"property\",\"text\":\"out\",\"displayText\":\"out\",\"tail\":\"PrintStream?\"}]";
        compareResult(1, 10, expectedResult, "java");
    }

    public void test$all$variable() throws IOException {
        String expectedResult = "[{\"icon\":\"property\",\"text\":\"stdin\",\"displayText\":\"stdin\",\"tail\":\"BufferedReader\"},{\"icon\":\"method\",\"text\":\"stream(\",\"displayText\":\"stream(() -> T?)\",\"tail\":\"Stream<T>\"},{\"icon\":\"method\",\"text\":\"stream(\",\"displayText\":\"stream(T, (T) -> T?)\",\"tail\":\"Stream<T>\"},{\"icon\":\"method\",\"text\":\"streamOf(\",\"displayText\":\"streamOf(Progression<T>)\",\"tail\":\"Stream<T>\"},{\"icon\":\"method\",\"text\":\"streamOf(\",\"displayText\":\"streamOf(vararg T)\",\"tail\":\"Stream<T>\"},{\"icon\":\"property\",\"text\":\"str\",\"displayText\":\"str\",\"tail\":\"String\"}]";
        compareResult(14, 24, expectedResult, "java");
        compareResult(14, 24, expectedResult, "js");
    }

    public void test$all$class() throws IOException {
        String expectedResult = "[{\"icon\":\"class\",\"text\":\"Greeter\",\"displayText\":\"Greeter\",\"tail\":\" (<root>)\"}]";
        compareResult(4, 3, expectedResult, "java");
        compareResult(4, 3, expectedResult, "js");
    }

    public void test$all$type$in$constructor() throws IOException {
        String expectedResult = "[{\"icon\":\"class\",\"text\":\"StrictMath\",\"displayText\":\"StrictMath\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"String\",\"displayText\":\"String\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"String\",\"displayText\":\"String\",\"tail\":\" (kotlin)\"},{\"icon\":\"class\",\"text\":\"StringBuffer\",\"displayText\":\"StringBuffer\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"StringBuilder\",\"displayText\":\"StringBuilder\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"StringCoding\",\"displayText\":\"StringCoding\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"StringIndexOutOfBoundsException\",\"displayText\":\"StringIndexOutOfBoundsException\",\"tail\":\" (java.lang)\"}]";
        compareResult(12, 28, expectedResult, "java");
        compareResult(12, 28, expectedResult, "js");
    }

    public void test$java$println() throws IOException {
        String expectedResult = "[{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Any?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Char)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(CharArray?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Double)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Float)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Int)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Long)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(String?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"printf(\",\"displayText\":\"printf(Locale?, String, vararg Any?)\",\"tail\":\"PrintStream\"},{\"icon\":\"method\",\"text\":\"printf(\",\"displayText\":\"printf(String, vararg Any?)\",\"tail\":\"PrintStream\"},{\"icon\":\"method\",\"text\":\"println()\",\"displayText\":\"println()\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Any?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Char)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(CharArray?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Double)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Float)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Int)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Long)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(String?)\",\"tail\":\"Unit\"}]";
        compareResult(1, 15, expectedResult, "java");
    }

    public void test$js$a() throws IOException {
        String expectedResult = "[{\"icon\":\"genericValue\",\"text\":\"args\",\"displayText\":\"args\",\"tail\":\"Array<String>\"},{\"icon\":\"method\",\"text\":\"array(\",\"displayText\":\"array(vararg T)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"text\":\"arrayList(\",\"displayText\":\"arrayList(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"text\":\"arrayListOf(\",\"displayText\":\"arrayListOf(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"text\":\"arrayOfNulls(\",\"displayText\":\"arrayOfNulls(Int)\",\"tail\":\"Array<T?>\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean, () -> Any)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean, Any)\",\"tail\":\"Unit\"}]";

        compareResult(2, 3, expectedResult, "js");
    }

    public void test$kt$a() throws IOException {
        String expectedResult = "[{\"icon\":\"genericValue\",\"text\":\"args\",\"displayText\":\"args\",\"tail\":\"Array<String>\"},{\"icon\":\"method\",\"text\":\"array(\",\"displayText\":\"array(vararg T)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"text\":\"arrayList(\",\"displayText\":\"arrayList(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"text\":\"arrayListOf(\",\"displayText\":\"arrayListOf(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"text\":\"arrayOfNulls(\",\"displayText\":\"arrayOfNulls(Int)\",\"tail\":\"Array<T?>\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean, () -> Any)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean, Any)\",\"tail\":\"Unit\"}]";
        compareResult(2, 3, expectedResult, "java");
    }

    private void compareResult(int lineNo, int charNo, String expectedResult, String runConfiguration) throws IOException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
        sessionInfo.setRunConfiguration(runConfiguration);
        PsiFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), "completion/root.kt", TestUtils.getDataFromFile(TestUtils.TEST_SRC, "completion/root.kt"));

        JsonResponseForCompletion responseForCompletion = new JsonResponseForCompletion(new ArrayList<PsiFile>(Collections.singletonList(currentPsiFile)), sessionInfo, currentPsiFile.getName(), lineNo, charNo);
        String actualResult = responseForCompletion.getResult();

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode expectedArray = (ArrayNode) objectMapper.readTree(expectedResult);
        ArrayNode actualArray = (ArrayNode) objectMapper.readTree(actualResult);

        assertEquals(expectedResult, actualResult);

        assertEquals("Incorrect count of objects in completion", expectedArray.size(), actualArray.size());


//        for (int i = 0; i < expectedArray.size(); i++) {
//            ObjectNode expectedObject = (ObjectNode) expectedArray.get(i);
//            for (int j = 0; j < actualArray.size(); j++) {
//                if (expectedObject.get("name").equals(actualArray.get(j).get("name"))) {
//                    assertEquals(expectedObject.toString(), actualArray.get(j).toString());
//                    break;
//                } else {
//                    if (j == actualArray.size() - 1) {
//                        assertEquals("Cannot find element for " + expectedObject.toString(), true, false);
//                    }
//                }
//            }
//        }

        assertEquals("Wrong result: " + lineNo + ", " + charNo, expectedResult, actualResult);
    }

}
