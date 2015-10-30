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

package org.jetbrains.webdemo.test.completion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.JetPsiFactoryUtil;
import org.jetbrains.webdemo.backend.responseHelpers.JsonResponseForCompletion;
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
        String expectedResult = "[{\"icon\":\"property\",\"text\":\"out\",\"displayText\":\"out\",\"tail\":\"PrintStream?\"},{\"icon\":\"\",\"text\":\"object\",\"displayText\":\"object\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"open\",\"displayText\":\"open\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"override\",\"displayText\":\"override\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"out\",\"displayText\":\"out\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"operator\",\"displayText\":\"operator\",\"tail\":\"\"}]";
        compareResult(1, 10, expectedResult, "java");
    }

    public void test$java$variable() throws IOException {
        String expectedResult = "[{\"icon\":\"method\",\"text\":\"sequence(\",\"displayText\":\"sequence(() -> T?)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequence(\",\"displayText\":\"sequence(() -> T?, (T) -> T?)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequence(\",\"displayText\":\"sequence(T, (T) -> T?)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequenceOf(\",\"displayText\":\"sequenceOf(Progression<T>)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequenceOf(\",\"displayText\":\"sequenceOf(vararg T)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"setOf()\",\"displayText\":\"setOf()\",\"tail\":\"Set<T>\"},{\"icon\":\"method\",\"text\":\"setOf(\",\"displayText\":\"setOf(T)\",\"tail\":\"Set<T>\"},{\"icon\":\"method\",\"text\":\"setOf(\",\"displayText\":\"setOf(vararg T)\",\"tail\":\"Set<T>\"},{\"icon\":\"method\",\"text\":\"shortArrayOf(\",\"displayText\":\"shortArrayOf(vararg Short)\",\"tail\":\"ShortArray\"},{\"icon\":\"method\",\"text\":\"sortedMapOf(\",\"displayText\":\"sortedMapOf(vararg Pair<K, V>)\",\"tail\":\"SortedMap<K, V>\"},{\"icon\":\"method\",\"text\":\"sortedSetOf(\",\"displayText\":\"sortedSetOf(Comparator<in T>, vararg T)\",\"tail\":\"TreeSet<T>\"},{\"icon\":\"method\",\"text\":\"sortedSetOf(\",\"displayText\":\"sortedSetOf(vararg T)\",\"tail\":\"TreeSet<T>\"},{\"icon\":\"property\",\"text\":\"stdin\",\"displayText\":\"stdin\",\"tail\":\"BufferedReader\"},{\"icon\":\"property\",\"text\":\"str\",\"displayText\":\"str\",\"tail\":\"String\"},{\"icon\":\"package\",\"text\":\"sun\",\"displayText\":\"sun\",\"tail\":\"package sun\"},{\"icon\":\"package\",\"text\":\"sunw\",\"displayText\":\"sunw\",\"tail\":\"package sunw\"},{\"icon\":\"method\",\"text\":\"synchronized(\",\"displayText\":\"synchronized(Any, () -> R)\",\"tail\":\"R\"},{\"icon\":\"\",\"text\":\"super\",\"displayText\":\"super\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"setparam\",\"displayText\":\"setparam\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"set\",\"displayText\":\"set\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"sealed\",\"displayText\":\"sealed\",\"tail\":\"\"}]";
        compareResult(14, 23, expectedResult, "java");}

    public void test$js$variable() throws IOException {
        String expectedResult = "[{\"icon\":\"method\",\"text\":\"safeParseDouble(\",\"displayText\":\"safeParseDouble(String)\",\"tail\":\"Double?\"},{\"icon\":\"method\",\"text\":\"safeParseInt(\",\"displayText\":\"safeParseInt(String)\",\"tail\":\"Int?\"},{\"icon\":\"method\",\"text\":\"sequence(\",\"displayText\":\"sequence(() -> T?)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequence(\",\"displayText\":\"sequence(() -> T?, (T) -> T?)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequence(\",\"displayText\":\"sequence(T, (T) -> T?)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequenceOf(\",\"displayText\":\"sequenceOf(Progression<T>)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"sequenceOf(\",\"displayText\":\"sequenceOf(vararg T)\",\"tail\":\"Sequence<T>\"},{\"icon\":\"method\",\"text\":\"setOf()\",\"displayText\":\"setOf()\",\"tail\":\"Set<T>\"},{\"icon\":\"method\",\"text\":\"setOf(\",\"displayText\":\"setOf(T)\",\"tail\":\"Set<T>\"},{\"icon\":\"method\",\"text\":\"setOf(\",\"displayText\":\"setOf(vararg T)\",\"tail\":\"Set<T>\"},{\"icon\":\"method\",\"text\":\"shortArrayOf(\",\"displayText\":\"shortArrayOf(vararg Short)\",\"tail\":\"ShortArray\"},{\"icon\":\"property\",\"text\":\"str\",\"displayText\":\"str\",\"tail\":\"String\"},{\"icon\":\"method\",\"text\":\"synchronized(\",\"displayText\":\"synchronized(Any, crossinline () -> R)\",\"tail\":\"R\"},{\"icon\":\"\",\"text\":\"super\",\"displayText\":\"super\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"setparam\",\"displayText\":\"setparam\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"set\",\"displayText\":\"set\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"sealed\",\"displayText\":\"sealed\",\"tail\":\"\"}]";
        compareResult(14, 23, expectedResult, "js");
    }

    public void test$all$class() throws IOException {
        String expectedResult = "[{\"icon\":\"class\",\"text\":\"GeneratorSequence\",\"displayText\":\"GeneratorSequence\",\"tail\":\" (kotlin)\"},{\"icon\":\"class\",\"text\":\"Greeter\",\"displayText\":\"Greeter\",\"tail\":\" (<root>)\"}]";
        compareResult(4, 3, expectedResult, "java");
        compareResult(4, 3, expectedResult, "js");
    }

    public void test$java$type$in$constructor() throws IOException {
        String expectedResult = "[{\"icon\":\"class\",\"text\":\"Strictfp\",\"displayText\":\"Strictfp\",\"tail\":\" (kotlin.jvm)\"},{\"icon\":\"class\",\"text\":\"StrictMath\",\"displayText\":\"StrictMath\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"String\",\"displayText\":\"String\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"String\",\"displayText\":\"String\",\"tail\":\" (kotlin)\"},{\"icon\":\"class\",\"text\":\"StringBuffer\",\"displayText\":\"StringBuffer\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"StringBuilder\",\"displayText\":\"StringBuilder\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"StringCoding\",\"displayText\":\"StringCoding\",\"tail\":\" (java.lang)\"},{\"icon\":\"class\",\"text\":\"StringIndexOutOfBoundsException\",\"displayText\":\"StringIndexOutOfBoundsException\",\"tail\":\" (java.lang)\"}]";
        compareResult(12, 28, expectedResult, "java");
    }

    public void test$js$type$in$constructor() throws IOException {
        String expectedResult = "[{\"icon\":\"class\",\"text\":\"String\",\"displayText\":\"String\",\"tail\":\" (kotlin)\"},{\"icon\":\"class\",\"text\":\"StringBuilder\",\"displayText\":\"StringBuilder\",\"tail\":\" (java.lang)\"}]";
        compareResult(12, 28, expectedResult, "js");
    }

    public void test$java$println() throws IOException {
        String expectedResult = "[{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Any?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Char)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(CharArray?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Double)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Float)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Int)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(Long)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"print(\",\"displayText\":\"print(String?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"printf(\",\"displayText\":\"printf(Locale?, String?, vararg Any?)\",\"tail\":\"PrintStream?\"},{\"icon\":\"method\",\"text\":\"printf(\",\"displayText\":\"printf(String?, vararg Any?)\",\"tail\":\"PrintStream?\"},{\"icon\":\"method\",\"text\":\"println()\",\"displayText\":\"println()\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Any?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Char)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(CharArray?)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Double)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Float)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Int)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(Long)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"println(\",\"displayText\":\"println(String?)\",\"tail\":\"Unit\"},{\"icon\":\"\",\"text\":\"package\",\"displayText\":\"package\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"property\",\"displayText\":\"property\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"param\",\"displayText\":\"param\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"private\",\"displayText\":\"private\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"public\",\"displayText\":\"public\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"protected\",\"displayText\":\"protected\",\"tail\":\"\"}]";
        compareResult(1, 15, expectedResult, "java");
    }

    public void test$js$a() throws IOException {
        String expectedResult = "[{\"icon\":\"genericValue\",\"text\":\"args\",\"displayText\":\"args\",\"tail\":\"Array<String>\"},{\"icon\":\"method\",\"text\":\"arrayCopyResize(\",\"displayText\":\"arrayCopyResize(Any?, Int, Any?)\",\"tail\":\"Any?\"},{\"icon\":\"method\",\"text\":\"arrayListOf(\",\"displayText\":\"arrayListOf(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"text\":\"arrayOf(\",\"displayText\":\"arrayOf(vararg T)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"text\":\"arrayOfNulls(\",\"displayText\":\"arrayOfNulls(Array<out T>, Int)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"text\":\"arrayOfNulls(\",\"displayText\":\"arrayOfNulls(Int)\",\"tail\":\"Array<T?>\"},{\"icon\":\"method\",\"text\":\"arrayPlusCollection(\",\"displayText\":\"arrayPlusCollection(Any?, Collection<T>)\",\"tail\":\"Any?\"},{\"icon\":\"\",\"text\":\"as\",\"displayText\":\"as\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"abstract\",\"displayText\":\"abstract\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"annotation\",\"displayText\":\"annotation\",\"tail\":\"\"}]";

        compareResult(2, 3, expectedResult, "js");
    }

    public void test$kt$a() throws IOException {
        String expectedResult = "[{\"icon\":\"genericValue\",\"text\":\"args\",\"displayText\":\"args\",\"tail\":\"Array<String>\"},{\"icon\":\"method\",\"text\":\"arrayListOf(\",\"displayText\":\"arrayListOf(vararg T)\",\"tail\":\"ArrayList<T>\"},{\"icon\":\"method\",\"text\":\"arrayOf(\",\"displayText\":\"arrayOf(vararg T)\",\"tail\":\"Array<T>\"},{\"icon\":\"method\",\"text\":\"arrayOfNulls(\",\"displayText\":\"arrayOfNulls(Array<out T>, Int)\",\"tail\":\"Array<out T>\"},{\"icon\":\"method\",\"text\":\"arrayOfNulls(\",\"displayText\":\"arrayOfNulls(Int)\",\"tail\":\"Array<T?>\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean, () -> Any)\",\"tail\":\"Unit\"},{\"icon\":\"method\",\"text\":\"assert(\",\"displayText\":\"assert(Boolean, Any)\",\"tail\":\"Unit\"},{\"icon\":\"\",\"text\":\"as\",\"displayText\":\"as\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"abstract\",\"displayText\":\"abstract\",\"tail\":\"\"},{\"icon\":\"\",\"text\":\"annotation\",\"displayText\":\"annotation\",\"tail\":\"\"}]";
        compareResult(2, 3, expectedResult, "java");
    }

    private void compareResult(int lineNo, int charNo, String expectedResult, String runConfiguration) throws IOException {
        BackendSessionInfo sessionInfo = new BackendSessionInfo("test", BackendSessionInfo.TypeOfRequest.COMPLETE);
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
