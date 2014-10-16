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

    public static String updateList() {
        response = new StringBuilder();
        exampleFoldersOrder = new ArrayList<>();
        exampleFolders = new HashMap<>();
        ExamplesList.getInstance().generateList();
        return response.toString();
    }

    public static String loadExample(String folderName, String exampleName) {
        folderName = folderName.replaceAll("_", " ");
        exampleName = exampleName.replaceAll("_", " ");

        ExamplesFolder folder = exampleFolders.get(folderName);
        Project example = folder.examples.get(exampleName);

        try {
            return objectMapper.writeValueAsString(example);
        } catch (IOException e) {
            return "";
        }
    }

    public static Project getExample(String url) {
        ExamplesFolder examplesFolder = exampleFolders.get(ResponseUtils.substringBefore(url, "&name=").replaceAll("_", " "));
        return examplesFolder.examples.get(ResponseUtils.substringAfter(url, "&name=").replaceAll("_", " "));
    }

    public static Project getExample(String name, String folder) {
        ExamplesFolder examplesFolder = exampleFolders.get(folder.replaceAll("_", " "));
        if (examplesFolder != null) {
            return examplesFolder.examples.get(name.replaceAll("_", " "));
        } else {
            return null;
        }
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
