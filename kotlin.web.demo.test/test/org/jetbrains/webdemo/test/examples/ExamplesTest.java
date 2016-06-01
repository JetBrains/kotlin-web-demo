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

import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.examples.Example;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.datastructures.TextPosition;
import org.jetbrains.webdemo.test.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

@RunWith(Parameterized.class)
public class ExamplesTest extends BaseTest {

    private static ArrayList<String> jsExamples = new ArrayList<String>();
    private final Example project;
    private String runConfiguration;


    public ExamplesTest(Example project) {
        super(project.name);
        this.project = project;
        this.runConfiguration = project.confType;
    }

//    public HighlightExamplesTest(Example project, String runConfiguration) {
//        super(project.name + "_" + runConfiguration);
//        this.project = project;
//        this.runConfiguration = runConfiguration;
//    }

//    @Override
    @Test
    public void testHighlighting() throws Throwable {
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

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        init();
        List<Object[]> parameters = new ArrayList<>();
        for (Example project : ExamplesUtils.getAllExamples(ExamplesFolder.ROOT_FOLDER)) {
            parameters.add(new Object[] {project});
        }
        return parameters;
    }
}

