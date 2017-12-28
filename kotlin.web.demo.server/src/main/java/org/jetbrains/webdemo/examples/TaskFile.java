/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package org.jetbrains.webdemo.examples;

import lombok.Getter;
import org.jetbrains.webdemo.ResponseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Prendota on 12/25/17
 */
public class TaskFile extends ExampleFile {

    @Getter
    private List<String> solutions;

    private TaskFile(TaskFile that) {
        super(that);
        solutions = that.solutions;
    }

    public TaskFile(String text, String solution, String id) {
        super("Task.kt", text, id, Type.KOTLIN_FILE, null, true, false);
        this.solutions = getSolutionsFromText(solution);
    }

    private List<String> getSolutionsFromText(String text) {
        List<String> solutions = new ArrayList<String>();
        String solution = ResponseUtils.substringBetween(text, "<answer>", "</answer>");
        while (!solution.equals("")) {
            solutions.add(solution);
            text = text.replaceFirst("<answer>", "").replaceFirst("</answer>", "");
            solution = ResponseUtils.substringBetween(text, "<answer>", "</answer>");
        }
        if (solutions.isEmpty()) solutions.add(text);
        return solutions;
    }

    @Override
    public ExampleFile copy() {
        return new TaskFile(this);
    }

}