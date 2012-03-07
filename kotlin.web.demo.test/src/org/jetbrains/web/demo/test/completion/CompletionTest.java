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

package org.jetbrains.web.demo.test.completion;

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.web.demo.test.BaseTest;
import org.jetbrains.web.demo.test.TestUtils;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForCompletion;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author Natalia.Ukhorskaya
 */

public class CompletionTest extends BaseTest {

    public void test$java$system() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"/static/icons/class.png\",\"name\":\"System : Any\",\"tail\":\"   \"}]";
        compareResult(1, 6, expectedResult, "java");
    }

    public void test$java$out() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"/static/icons/property.png\",\"name\":\"out\",\"tail\":\"   PrintStream?\"}]";
        compareResult(1, 10, expectedResult, "java");
    }

    public void test$java$println() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Boolean)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Float)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Int)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Long)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Double)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Char)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"printf(p0 : Locale?, p1 : String?, p2 : ...\",\"tail\":\"   PrintStream?\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Any?)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Double)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : String?)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : CharArray?)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Boolean)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"printf(p0 : String?, p1 : Array<Any?>?)\",\"tail\":\"   PrintStream?\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Float)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : CharArray?)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Char)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println()\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Any?)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : Long)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"print(p0 : String?)\",\"tail\":\"   Unit\"},{\"icon\":\"/static/icons/method.png\",\"name\":\"println(p0 : Int)\",\"tail\":\"   Unit\"}]";

        compareResult(1, 15, expectedResult, "java");
    }

    public void test$kt$a() throws IOException, JSONException {
        String expectedResult = "[{\"icon\":\"/static/icons/genericValue.png\",\"name\":\"args : Array<String>\",\"tail\":\"   Array<String>\"},{\"icon\":\"/static/icons/class.png\",\"name\":\"atomic : Any\",\"tail\":\"   \"},{\"icon\":\"/static/icons/method.png\",\"name\":\"array(t : Array<T>)\",\"tail\":\"   Array<T>\"}]";
        compareResult(2, 3, expectedResult, "java");
    }

    private void compareResult(int start, int end, String expectedResult, String runConfiguration) throws IOException, JSONException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
        sessionInfo.setRunConfiguration(runConfiguration);
        JetFile currentPsiFile = JetPsiFactory.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, "completion/root.kt"));

        JsonResponseForCompletion responseForCompletion = new JsonResponseForCompletion(start, end, currentPsiFile, sessionInfo);
        String actualResult = responseForCompletion.getResult();
        JSONArray expectedArray = new JSONArray(expectedResult);
        JSONArray actualArray = new JSONArray(actualResult);

//        assertEquals(expectedResult, actualResult);

        assertEquals("Incorrect count of objects in completion", expectedArray.length(), actualArray.length());

        System.out.println(expectedResult + "\n" + actualResult);

        for (int i = 0; i < expectedArray.length(); i++) {
            JSONObject expectedObject = (JSONObject) expectedArray.get(i);
            for (int j = 0; j < actualArray.length(); j++) {
                if (expectedObject.get("name").equals(((JSONObject) actualArray.get(j)).get("name"))) {
                    assertEquals(expectedObject.toString(), actualArray.get(j).toString());
                    break;
                } else {
                    if (j == actualArray.length() - 1) {
                        assertEquals("Cannot find element for " + expectedObject.toString(), true, false);
                    }
                }
            }
            //assertEquals("Wrong result", expectedArray.get(i).toString(), actualArray.get(i).toString());
        }

    }

}
