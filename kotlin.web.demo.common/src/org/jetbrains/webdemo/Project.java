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

package org.jetbrains.webdemo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Project {
    public String originUrl;
    public String name;
    public String parent;
    public String args = "";
    public String confType = "java";

    public String help = "";
    public List<ProjectFile> files;
    public List<String> readOnlyFileNames = new ArrayList<>();
    private String expectedOutput;

    /**
     * For Jackson
     */
    public Project() {

    }

    public Project( String name,
                    String parent,
                    String args,
                    String confType) {
        this.name = name;
        this.parent = parent;
        this.args = args;
        this.confType = confType;
        this.originUrl = null;
        this.files = new ArrayList<>();
    }

    /**
     * This constructor is used to get examples from database.
     *
     * @param name      - Project name
     * @param parent    - Project folder
     * @param args      - Project args
     * @param confType  - Project run configuration
     * @param originUrl - Project origin URL
     */
    public Project( String name,
                    String parent,
                    String args,
                    String confType,
                   String originUrl,
                   List<String> readOnlyFileNames) {
        this.name = name;
        this.parent = parent;
        this.args = args;
        this.confType = confType;
        this.originUrl = originUrl;
        this.files = new ArrayList<>();
        this.readOnlyFileNames = readOnlyFileNames;
    }

    /**
     * This constructor is used to get examples from server.
     *
     * @param parent            - Name of parent folder
     * @param exampleFolderName - Name of example folder
     * @throws IOException - if example folder has no manifest file, if example files do not exists, or some other problem with IO.
     */
    public Project( String parent,  String exampleFolderName, String examplesDirectory, boolean loadTestVersion) throws IOException {
        this.files = new ArrayList<>();
        this.parent = parent;

        originUrl = "folder=" + parent.replaceAll(" ", "%20") + "&project=" + exampleFolderName.replaceAll(" ", "%20");
        ObjectMapper objectMapper = new ObjectMapper();

        String exampleFolderPath = examplesDirectory + File.separator + parent + File.separator + exampleFolderName + File.separator;
        File manifest = new File(exampleFolderPath + "manifest.json");
        JsonNode objectNode = objectMapper.readTree(manifest);

        name = objectNode.get("name").asText();
        args = objectNode.get("args").asText();
        confType = objectNode.get("confType").asText();
        if (objectNode.has("expectedOutput")) {
            expectedOutput = objectNode.get("expectedOutput").asText();
        } else if (objectNode.has("expectedOutputFile")) {
            Path path = Paths.get(exampleFolderPath + File.separator + objectNode.get("expectedOutputFile").asText());
            expectedOutput = new String(Files.readAllBytes(path));
        }

        help = objectNode.get("help").textValue();
        ArrayNode fileDescriptors = (ArrayNode) objectNode.get("files");
        for (JsonNode fileDescriptor : fileDescriptors) {
            if(loadTestVersion &&
                    fileDescriptor.has("skipInTestVersion") &&
                    fileDescriptor.get("skipInTestVersion").asBoolean()){
                continue;
            }
            ProjectFile file = deserializeProjectFile(parent, exampleFolderPath, fileDescriptor);
            if(!loadTestVersion && file.getType().equals(ProjectFile.Type.SOLUTION_FILE)) {
                continue;
            }
            files.add(file);
        }
    }

    private ProjectFile deserializeProjectFile(String parent, String exampleFolderPath, JsonNode fileDescriptor) throws IOException {
        String fileName = fileDescriptor.get("filename").textValue();
        boolean modifiable = fileDescriptor.get("modifiable").asBoolean();
        if (!modifiable) {
            readOnlyFileNames.add(fileName);
        }
        String fileContent;
        Path path = Paths.get(exampleFolderPath + File.separator + fileName);
        fileContent = new String(Files.readAllBytes(path)).replaceAll("\r\n", "\n");

        String filePublicId = "folder=" + parent + "&project=" + name + "&file=" + fileName;
        ProjectFile.Type fileType = null;
        if(!fileDescriptor.has("type")){
            fileType = ProjectFile.Type.KOTLIN_FILE;
        } else if(fileDescriptor.get("type").asText().equals("kotlin-test")){
            fileType = ProjectFile.Type.KOTLIN_TEST_FILE;
        } else if(fileDescriptor.get("type").asText().equals("solution")){
            fileType = ProjectFile.Type.SOLUTION_FILE;
        }
        return new ProjectFile(fileName, fileContent, modifiable, filePublicId, fileType);
    }

    @JsonIgnore
    public String getExpectedOutput() {
        return expectedOutput;
    }
}
