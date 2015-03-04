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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.webdemo.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamplesList {
    private static final ExamplesList EXAMPLES_LIST = new ExamplesList();

    private ExamplesList() {
        ExamplesFolder.ROOT_FOLDER = loadFolder(ApplicationSettings.EXAMPLES_DIRECTORY);
    }

    public static ExamplesList getInstance() {
        return EXAMPLES_LIST;
    }

    public void addUnmodifiableFilesToProject(Project project) {
        if (project.originUrl != null) {
            Project storedExample = ExamplesList.getInstance().getExample(project.originUrl);
            for (ProjectFile file : storedExample.files) {
                if (!file.isModifiable() && project.readOnlyFileNames.contains(file.getName())) {
                    project.files.add(file);
                }
            }
        }
    }

    public Project getExample(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String folderName = ResponseUtils.substringBetween(url, "folder=", "&project=");
        String exampleName = ResponseUtils.substringAfter(url, "&project=");
        return ExamplesFolder.ROOT_FOLDER.childFolders.get(folderName).examples.get(exampleName);
    }

    public ProjectFile getExampleFile(String url) {
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

    public List<Project> getAllExamples() {
        List<Project> examples = new ArrayList<>();
        for (ExamplesFolder folder : ExamplesFolder.ROOT_FOLDER.childFolders.values()) {
            examples.addAll(folder.examples.values());
        }
        return examples;
    }

    private ExamplesFolder loadFolder(String path) {
        File manifestFile = new File(path + File.separator + "manifest.json");
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(manifestFile))) {
            ObjectNode manifest = (ObjectNode) JsonUtils.getObjectMapper().readTree(reader);
            String name = new File(path).getName();
            Map<String, Project> examples = new LinkedHashMap<>();
            Map<String, ExamplesFolder> childFolders = new LinkedHashMap<>();

            if (manifest.has("folders")) {
                for (JsonNode node : manifest.get("folders")) {
                    String folderName = node.textValue();
                    childFolders.put(folderName, loadFolder(path + File.separator + folderName));
                }
            }

            if (manifest.has("examples")) {
                for (JsonNode node : manifest.get("examples")) {
                    String projectName = node.textValue();
                    examples.put(projectName, loadProject(path + File.separator + projectName, ApplicationSettings.LOAD_TEST_VERSION_OF_EXAMPLES));
                }
            }

            return new ExamplesFolder(name, examples, childFolders);
        } catch (IOException e) {
            System.err.println("Can't load folder: " + e.toString());
            return null;
        }
    }

    private Project loadProject(String path, boolean loadTestVersion) throws IOException {
        File manifestFile = new File(path + File.separator + "manifest.json");
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(manifestFile))) {
            ObjectNode manifest = (ObjectNode) JsonUtils.getObjectMapper().readTree(reader);

            String parent = new File(path).getParentFile().getName();
            String name = new File(path).getName();
            String originUrl = "folder=" + parent.replaceAll(" ", "%20") + "&project=" + name.replaceAll(" ", "%20");
            String args = manifest.get("args").asText();
            String runConfiguration = manifest.get("confType").asText();
            String help = manifest.get("help").textValue();
            String expectedOutput;
            List<String> readOnlyFileNames = new ArrayList<>();
            List<ProjectFile> files = new ArrayList<>();
            if (manifest.has("expectedOutput")) {
                expectedOutput = manifest.get("expectedOutput").asText();
            } else if (manifest.has("expectedOutputFile")) {
                Path expectedOutputFilePath = Paths.get(path + File.separator + manifest.get("expectedOutputFile").asText());
                expectedOutput = new String(Files.readAllBytes(expectedOutputFilePath));
            } else {
                expectedOutput = null;
            }


            ArrayNode fileDescriptors = (ArrayNode) manifest.get("files");
            for (JsonNode fileDescriptor : fileDescriptors) {

                if (loadTestVersion &&
                        fileDescriptor.has("skipInTestVersion") &&
                        fileDescriptor.get("skipInTestVersion").asBoolean()) {
                    continue;
                }

                ProjectFile file = loadProjectFile(path, originUrl, fileDescriptor);
                if (!loadTestVersion && file.getType().equals(ProjectFile.Type.SOLUTION_FILE)) {
                    continue;
                }
                if (!file.isModifiable()) {
                    readOnlyFileNames.add(file.getName());
                }

                files.add(file);
            }

            return new Project(name, parent, args, runConfiguration, originUrl, expectedOutput, help, files, readOnlyFileNames);
        } catch (IOException e) {
            System.err.println("Can't load project: " + e.toString());
            return null;
        }
    }


    private ProjectFile loadProjectFile(String path, String projectUrl, JsonNode fileDescriptor) throws IOException {
        try {
            String fileName = fileDescriptor.get("filename").textValue();
            boolean modifiable = fileDescriptor.get("modifiable").asBoolean();
            File file = new File(path + File.separator + fileName);
            String fileContent = new String(Files.readAllBytes(file.toPath())).replaceAll("\r\n", "\n");
            String filePublicId = (projectUrl + "&file=" + fileName).replaceAll(" ", "%20");
            ProjectFile.Type fileType = null;
            if (!fileDescriptor.has("type")) {
                fileType = ProjectFile.Type.KOTLIN_FILE;
            } else if (fileDescriptor.get("type").asText().equals("kotlin-test")) {
                fileType = ProjectFile.Type.KOTLIN_TEST_FILE;
            } else if (fileDescriptor.get("type").asText().equals("solution")) {
                fileType = ProjectFile.Type.SOLUTION_FILE;
            } else if (fileDescriptor.get("type").asText().equals("java")) {
                fileType = ProjectFile.Type.JAVA_FILE;
            }
            return new ProjectFile(fileName, fileContent, modifiable, filePublicId, fileType);
        } catch (IOException e) {
            System.err.println("Can't load file: " + e.toString());
            return null;
        }
    }

}
