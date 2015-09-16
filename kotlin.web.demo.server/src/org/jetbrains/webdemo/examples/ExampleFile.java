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

package org.jetbrains.webdemo.examples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.ResponseUtils;

import java.util.ArrayList;
import java.util.List;

public class ExampleFile extends ProjectFile {
    private boolean hidden;
    @Nullable
    private String confType;
    private List<TextRange> taskWindows;

    public ExampleFile(String name, String text, String id, Type type, @Nullable String confType, boolean isModifiable, boolean isHidden) {
        super(name, text, isModifiable, id, type);
        hidden = isHidden;
        this.confType = confType;
        this.taskWindows = getTaskWindowsFromText();
    }

    private List<TextRange> getTaskWindowsFromText() {
        List<TextRange> textRanges = new ArrayList<>();
        String[] fileContentLines = text.split("\\r?\\n");
        for (int i = 0; i < fileContentLines.length; i++) {
            String line = fileContentLines[i];
            while (line.contains("<taskWindow>")) {
                int taskWindowStart = line.indexOf("<taskWindow>");
                line = line.replace("<taskWindow>", "");
                int taskWindowEnd = line.indexOf("</taskWindow>");
                line = line.replace("</taskWindow>", "");
                textRanges.add(new TextRange(i, taskWindowStart, taskWindowEnd - taskWindowStart));
            }
        }
        text = text.replaceAll("<taskWindow>", "").replaceAll("</taskWindow>", "");
        return textRanges;
    }

    @JsonIgnore
    public boolean isHidden() {
        return hidden;
    }

    @JsonIgnore
    @Nullable
    public String getConfType() {
        return confType;
    }

    public List<TextRange> getTaskWindows() {
        return taskWindows;
    }
}

class TaskFile extends ExampleFile {
    private List<String> solutions;

    public TaskFile(String text, String solution, String id) {
        super("Task.kt", text, id, Type.KOTLIN_FILE, null, true, false);
        this.solutions = getSolutionsFromText(solution);
    }

    private List<String> getSolutionsFromText(String text){
        List<String> solutions = new ArrayList<String>();
        String solution = ResponseUtils.substringBetween(text, "<answer>", "</answer>");
        while(!solution.equals("")){
            solutions.add(solution);
            text = text.replaceFirst("<answer>", "").replaceFirst("</answer>", "");
            solution = ResponseUtils.substringBetween(text, "<answer>", "</answer>");
        }
        return solutions;
    }

    public List<String> getSolutions() {
        return solutions;
    }
}

class TestFile extends ExampleFile {
    public TestFile(String text, String id) {
        super("Test.kt", text, id, Type.KOTLIN_TEST_FILE, null, false, false);
    }
}

class SolutionFile extends ExampleFile {
    public SolutionFile(String text, String id) {
        super(
                "Solution.kt",
                text.replaceAll("<answer>", "").replaceAll("</answer>", ""),
                id,
                Type.SOLUTION_FILE,
                null,
                false,
                false);
    }
}

class TextRange {
    private int line;
    private int start;
    private int length;

    public TextRange(int line, int start, int length) {
        this.line = line;
        this.start = start;
        this.length = length;
    }

    public int getLine() {
        return line;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }
}
