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

import org.jetbrains.jet.internal.com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamplesList {
    private static final ExamplesList EXAMPLES_LIST = new ExamplesList();

    private static StringBuilder response;

    private ExamplesList() {
        response = new StringBuilder();
        list = new ArrayList<Map<String, String>>();
        generateList();
    }

    private static List<Map<String, String>> list;

    public static ExamplesList getInstance() {
        return EXAMPLES_LIST;
    }

    public List<Map<String, String>> getList() {
        return list;
    }

    private void generateList() {
        File root = new File(ApplicationSettings.EXAMPLES_DIRECTORY);
        if (root.exists()) {
            File order = checkIsOrderTxtExists(root);
            if (order != null) {
                addInOrder(order, root, true);
            } else {
                addWoOrder(root, true);
            }
        } else {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Examples root doesn't exists"),
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), root.getAbsolutePath());
            ErrorWriter.writeErrorToConsole("Examples root doesn't exists");
            response.append("\nExamples root doesn't exists");
        }
        ErrorWriter.writeInfoToConsole("Examples were loaded.");
        response.append("\nExamples were loaded.");
    }

    private void addWoOrder(File parent, boolean isDirectory) {
        File[] children = parent.listFiles();
        if (children == null) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Incorrect structure for examples (folder - files): empty child list"),
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), parent.getAbsolutePath());
            return;
        }
        for (File child : children) {
            if ((parent.isDirectory() && isDirectory)
                    || (parent.exists() && !isDirectory)) {
                Map<String, String> map = new HashMap<String, String>();
                if (isDirectory) {
                    map.put("type", "folder");
                    map.put("text", child.getName());
                } else {
                    map.put("type", "content");
                    if (child.getName().endsWith(".kt")) {
                        map.put("text", child.getName().substring(0, child.getName().length() - 3));
                    } else {
                        map.put("text", child.getName());
                    }

                    ExampleObject example = new ExampleObject();
                    example.fileName = map.get("text");
                    example.name = map.get("text");
                    example.parent = parent.getName();

                    ExamplesHolder.addExample(map.get("text"), example);
                }

                list.add(map);

                if (isDirectory) {
                    File order = new File(child.getAbsolutePath() + File.separator + "order.txt");
                    if (order.exists()) {
                        addInOrder(order, child, false);
                    } else {
                        addWoOrder(child, false);
                    }
                }
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                        new UnsupportedOperationException("Incorrect structure for examples (folder - files)."),
                        SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), child.getAbsolutePath());
            }
        }
    }

    private void addInOrder(File order, File parent, boolean isDirectory) {
        try {
            String[] children = parent.list();
            FileReader fReader = new FileReader(order);
            BufferedReader reader = new BufferedReader(fReader);
            String tmp = "";
            List<String> orderedChildren = new ArrayList<String>();
            while ((tmp = reader.readLine()) != null) {
                File child = new File(parent.getAbsolutePath() + File.separator + tmp);
                if ((child.isDirectory() && isDirectory)
                        || (child.exists() && !isDirectory)) {
                    Map<String, String> map = new HashMap<String, String>();
                    if (isDirectory) {
                        map.put("type", "folder");
                        map.put("text", child.getName());
                    } else {
                        map.put("type", "content");
                        if (child.getName().endsWith(".kt")) {
                            map.put("text", child.getName().substring(0, child.getName().length() - 3));
                        } else {
                            map.put("text", child.getName());
                        }

                        ExampleObject example = new ExampleObject();
                        example.fileName = map.get("text");
                        example.name = map.get("text");
                        example.parent = parent.getName();

                        ExamplesHolder.addExample(map.get("text"), example);
                    }
                    list.add(map);

                    orderedChildren.add(child.getName());

                    if (isDirectory) {
                        File orderChildren = checkIsOrderTxtExists(child);
                        if (orderChildren != null) {
                            addInOrder(orderChildren, child, false);
                        } else {
                            addWoOrder(child, false);
                        }
                    }
                }
            }
            //+1 for order.txt
            if (orderedChildren.size() + 1 < children.length) {
                for (String childName : children) {
                    if (!childName.equals("order.txt") && !childName.equals("helpExamples.xml")) {
                        boolean isAdded = false;
                        for (String orderedChild : orderedChildren) {
                            if (childName.equals(orderedChild)) {
                                isAdded = true;
                            }
                        }
                        if (!isAdded) {
                            File child = new File(parent.getAbsolutePath() + File.separator + childName);
                            if ((child.isDirectory() && isDirectory)
                                    || (child.exists() && !isDirectory)) {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("text", child.getName());
                                if (isDirectory) {
                                    map.put("type", "folder");
                                } else {
                                    map.put("type", "content");

                                    ExampleObject example = new ExampleObject();
                                    example.fileName = map.get("text");
                                    example.name = map.get("text");
                                    example.parent = parent.getName();

                                    ExamplesHolder.addExample(map.get("text"), example);
                                }
                                list.add(map);

                                ErrorWriter.writeErrorToConsole("File/Directory " + childName + " is absent in order.txt and was added at end.");
                                response.append("\nFile/Directory ").append(childName).append(" is absent in order.txt and was added at end.");

                                if (isDirectory) {
                                    File orderChildren = checkIsOrderTxtExists(child);
                                    if (orderChildren != null) {
                                        addInOrder(orderChildren, child, false);
                                    } else {
                                        addWoOrder(child, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), order.getAbsolutePath());
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot read order.txt file"),
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), order.getAbsolutePath());
        }
    }

    @Nullable
    private File checkIsOrderTxtExists(File root) {
        File order = new File(root.getAbsolutePath() + File.separator + "order.txt");
        if (order.exists()) {
            return order;
        }
        return null;
    }

    public static String updateList() {
        response = new StringBuilder();
        list = new ArrayList<Map<String, String>>();
        ExamplesList.getInstance().generateList();
        return response.toString();
    }
}
