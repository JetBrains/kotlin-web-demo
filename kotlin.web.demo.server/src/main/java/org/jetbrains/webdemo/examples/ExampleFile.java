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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ProjectFile;

import java.util.ArrayList;
import java.util.List;

public class ExampleFile extends ProjectFile {

    private boolean hidden;

    @Nullable
    private String confType;

    @Nullable
    private String userText;

    @Getter
    private List<TestRange> taskWindows;

    ExampleFile(ExampleFile other) {
        super(other);
        hidden = other.hidden;
        confType = other.confType;
        userText = other.userText;
        taskWindows = other.taskWindows;
    }

    public ExampleFile(String name, String text, String id, Type type, @Nullable String confType, boolean isModifiable, boolean isHidden) {
        super(name, text, isModifiable, id, type);
        hidden = isHidden;
        this.confType = confType;
        this.taskWindows = getTaskWindowsFromText();
        userText = null;
    }

    private List<TestRange> getTaskWindowsFromText() {
        List<TestRange> textRanges = new ArrayList<>();
        String[] fileContentLines = text.split("\\r?\\n");
        for (int i = 0; i < fileContentLines.length; i++) {
            String line = fileContentLines[i];
            while (line.contains("<taskWindow>")) {
                int taskWindowStart = line.indexOf("<taskWindow>");
                line = line.replace("<taskWindow>", "");
                int taskWindowEnd = line.indexOf("</taskWindow>");
                line = line.replace("</taskWindow>", "");
                textRanges.add(new TestRange(i, taskWindowStart, taskWindowEnd - taskWindowStart));
            }
        }
        text = text.replaceAll("<taskWindow>", "").replaceAll("</taskWindow>", "");
        return textRanges;
    }

    public ExampleFile copy() {
        return new ExampleFile(this);
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

    @Nullable
    public String getUserText() {
        return userText;
    }

    public void setUserText(@Nullable String userText) {
        this.userText = userText;
    }
}

