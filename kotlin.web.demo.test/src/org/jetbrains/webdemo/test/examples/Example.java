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

import org.jetbrains.webdemo.test.TestUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Natalia.Ukhorskaya
 */

public class Example {
    private String fileName;
    private String result;
    private String args;

    public Example(String fileName, String args, String result) {
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
                resultFromFile = TestUtils.escape(resultFromFile) + "<br/>";
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
