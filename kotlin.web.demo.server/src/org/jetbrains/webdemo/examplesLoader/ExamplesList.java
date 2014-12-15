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

package org.jetbrains.webdemo.examplesLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamplesList {
    private static final ExamplesList EXAMPLES_LIST = new ExamplesList();

    private static StringBuilder response;
    private static ObjectMapper objectMapper;
    private static List<String> exampleFoldersOrder;
    private static Map<String, ExamplesFolder> exampleFolders;

    private ExamplesList() {
        response = new StringBuilder();
        exampleFoldersOrder = new ArrayList<>();
        exampleFolders = new HashMap<>();
        objectMapper = new ObjectMapper();
        generateList();
    }

    public static ExamplesList getInstance() {
        return EXAMPLES_LIST;
    }

    public static Project getExample(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String folderName = ResponseUtils.substringBetween(url, "folder=", "&project=");
        String exampleName = ResponseUtils.substringAfter(url, "&project=");
        return exampleFolders.get(folderName).examples.get(exampleName);
    }

    public static ProjectFile getExampleFile(String url) {
        url = ResponseUtils.unEscapeURL(url);
        String folderName = ResponseUtils.substringBetween(url, "folder=", "&project=");
        String exampleName = ResponseUtils.substringBetween(url, "&project=", "&file=");
        String fileName = ResponseUtils.substringAfter(url, "&file=");

        Project example = exampleFolders.get(folderName).examples.get(exampleName);
        for (ProjectFile file : example.files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        throw new NullPointerException("File not found");
    }

    public List<String> getOrderedFolderNames() {
        return exampleFoldersOrder;
    }

    public ExamplesFolder getFolder(String name) {
        return exampleFolders.get(name);
    }

    private void generateList() {
        File order = new File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + "order");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(order));
            while (reader.ready()) {
                String folderName = reader.readLine();
                File manifest = new File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + folderName + File.separator + "manifest.json");
                try {
                    ExamplesFolder examplesFolder = objectMapper.readValue(manifest, ExamplesFolder.class);
                    exampleFoldersOrder.add(folderName);
                    exampleFolders.put(folderName, examplesFolder);
                } catch (Exception e) {
                    System.err.println("Can't load folder " + folderName + ":\n" + e.getMessage());
                    response.append("Can't load folder " + folderName + ":\n" + e.getMessage());
                }

            }
        } catch (IOException e) {
            System.err.println("Can't read order:\n" + e.getMessage());
            response.append("Can't read order:\n" + e.getMessage());
        }
    }

}
