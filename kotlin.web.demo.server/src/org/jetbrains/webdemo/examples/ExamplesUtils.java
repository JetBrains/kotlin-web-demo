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

import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.ResponseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon.Atamas on 3/4/2015.
 */
public class ExamplesUtils {
    public static Project getExample(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String folderName = ResponseUtils.substringBetween(url, "folder=", "&project=");
        String exampleName = ResponseUtils.substringAfter(url, "&project=");
        return ExamplesFolder.ROOT_FOLDER.childFolders.get(folderName).examples.get(exampleName);
    }

    public static void addUnmodifiableFilesToProject(Project project) {
        if (project.originUrl != null) {
            Project storedExample = getExample(project.originUrl);
            for (ProjectFile file : storedExample.files) {
                if (!file.isModifiable() && project.readOnlyFileNames.contains(file.getName())) {
                    project.files.add(file);
                }
            }
        }
    }

    public static ProjectFile getExampleFile(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String folderName = ResponseUtils.substringBetween(url, "folder=", "&project=");
        String exampleName = ResponseUtils.substringBetween(url, "&project=", "&file=");
        String fileName = ResponseUtils.substringAfter(url, "&file=");

        Project example = ExamplesFolder.ROOT_FOLDER.childFolders.get(folderName).examples.get(exampleName);
        for (ProjectFile file : example.files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        throw new NullPointerException("File not found");
    }

    public static List<Project> getAllExamples() {
        List<Project> examples = new ArrayList<>();
        for (ExamplesFolder folder : ExamplesFolder.ROOT_FOLDER.childFolders.values()) {
            examples.addAll(folder.examples.values());
        }
        return examples;
    }
}
