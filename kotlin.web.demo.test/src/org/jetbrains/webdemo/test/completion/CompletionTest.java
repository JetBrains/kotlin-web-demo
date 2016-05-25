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

import org.jetbrains.webdemo.kotlin.datastructures.CompletionVariant;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompletionTest extends BaseTest {

    public void test$java$system() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "System",
                "System",
                " (java.lang)",
                "class"
        ));
        compareResult(1, 6, expectedVariants, false);
    }

    public void test$java$out() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "out",
                "out",
                "PrintStream?",
                "property"
        ));
        compareResult(1, 10, expectedVariants, false);
    }
//
    public void test$all$variable() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "str",
                "str",
                "String",
                "property"
        ));
        compareResult(14, 23, expectedVariants, false);
        compareResult(14, 23, expectedVariants, true);
    }

    public void test$all$class() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "Greeter",
                "Greeter",
                " (<root>)",
                "class"
        ));
        compareResult(4, 3, expectedVariants, false);
        compareResult(4, 3, expectedVariants, true);
    }

    public void test$java$type$in$constructor() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "String",
                "String",
                " (java.lang)",
                "class"
        ));
        compareResult(12, 28, expectedVariants, false);
    }

    public void test$js$type$in$constructor() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "String",
                "String",
                " (kotlin)",
                "class"
        ));
        compareResult(12, 28, expectedVariants, true);
    }
    public void test$java$println() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "println()",
                "println()",
                "Unit",
                "method"
        ));
        expectedVariants.add(new CompletionVariant(
                "println(",
                "println(Any?)",
                "Unit",
                "method"
        ));
        expectedVariants.add(new CompletionVariant(
                "println(",
                "println(String?)",
                "Unit",
                "method"
        ));
        compareResult(1, 20, expectedVariants, false);
    }

    public void test$js$a() throws IOException {
        List<CompletionVariant> expectedVariants = new ArrayList<>();
        expectedVariants.add(new CompletionVariant(
                "args",
                "args",
                "Array<String>",
                "genericValue"
        ));
        compareResult(2, 3, expectedVariants, false);
        compareResult(2, 3, expectedVariants, true);
    }

    private void compareResult(int line, int ch, List<CompletionVariant> expectedVariants, boolean isJs) throws IOException {
        Map<String, String> files = new HashMap<>();
        files.put("root.kt", TestUtils.getDataFromFile("completion/root.kt"));
        List<CompletionVariant> result = kotlinWrapper.getCompletionVariants(files, "root.kt", line, ch, isJs);
        for(CompletionVariant variant : expectedVariants) {
            assertTrue(result.contains(variant));
        }
    }

}
