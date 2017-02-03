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
import org.jetbrains.webdemo.database.DatabaseOperationException;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.session.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExamplesUtils {
    public static Example getExample(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String[] path = url.split("/");
        return getExample(path);
    }

    public static Example getUserVersionOfExample(UserInfo userInfo, String url) throws DatabaseOperationException {
        Example example = getExample(url);
        Project userVersion = MySqlConnector.getInstance().getStoredSolution(userInfo, url);
        if(userVersion == null) return example;

        List<ProjectFile> userFiles = new ArrayList<ProjectFile>();
        for(ProjectFile file : example.files){
            ProjectFile resultFile = file;
            for(ProjectFile userFile : userVersion.files){
                if (userFile.getName().equals(file.getName())){
                    resultFile = file.copy();
                    ((ExampleFile) resultFile).setUserText(userFile.getText());
                }
            }
            userFiles.add(resultFile);
        }
        return new Example(
                example.id,
                example.name,
                example.args,
                example.confType,
                example.originUrl,
                example.expectedOutput,
                example.shouldSearchForMain(),
                userFiles,
                example.getHiddenFiles(),
                example.readOnlyFileNames,
                example.getHelp()
        );
    }

    private static Example getExample(String[] path) {
        ExamplesFolder folder = ExamplesFolder.ROOT_FOLDER;
        for (int i = 1; i < path.length - 1; ++i) {
            folder = folder.getChildFolder(path[i]);
        }
        return folder.getExample(path[path.length - 1]);
    }

    public static void addUnmodifiableFilesToProject(Project project) {
        if (project.originUrl != null) {
            Project storedExample = getExample(project.originUrl);
            for (ProjectFile file : storedExample.files) {
                if (file.isModifiable()) continue;
                if (!project.readOnlyFileNames.contains(file.getName())) continue;
                String confType = ((ExampleFile) file).getConfType();
                if (confType != null && !project.confType.equals(confType)) continue;
                project.files.add(file);
            }
        }
    }

    public static void addHiddenFilesToProject(Project project) {
        if (project.originUrl == null) return;
        if(!project.originUrl.equals(project.id)) return;
        Example storedExample = getExample(project.id);
        for(ProjectFile file : storedExample.getHiddenFiles()){
            String confType = ((ExampleFile) file).getConfType();
            if (confType != null && !project.confType.equals(confType)) continue;
            project.files.add(file);
        }
    }

    public static ProjectFile getExampleFile(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String[] path = url.split("/");

        Project example = getExample(Arrays.copyOfRange(path, 0, path.length - 1));
        for (ProjectFile file : example.files) {
            if (file.getName().equals(path[path.length - 1])) {
                return file;
            }
        }
        throw new NullPointerException("File not found");
    }

    public static List<Example> getAllExamples(ExamplesFolder folder) {
        List<Example> examples = new ArrayList<>();
        examples.addAll(folder.getExamples());
        for (ExamplesFolder childFolder : folder.getChildFolders()) {
            examples.addAll(getAllExamples(childFolder));
        }
        return examples;
    }
}
