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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Natalia.Ukhorskaya
 */

public class ExamplesHolder {

    private static final Map<String, ExampleObject> allExamples = new HashMap<String, ExampleObject>();

    private ExamplesHolder() {

    }

    private static ExamplesHolder instance = new ExamplesHolder();

    public static ExamplesHolder getInstance() {
        return instance;
    }

    public static void addExample(String name, ExampleObject example) {
        allExamples.put(name, example);
    }

    @Nullable
    public static ExampleObject getExample(String name) {
        return allExamples.get(name);
    }

    public static String loadExample(String url) {
        url = url.replaceAll("_", " ");
        JSONArray array = new JSONArray();
        Map<String, String> resultMap = new HashMap<String, String>();
        String exampleName = ResponseUtils.getExampleOrProgramNameByUrl(url);

        ExampleObject example = allExamples.get(exampleName);

        File exampleFile = new File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + example.parent + File.separator + example.fileName + ".kt");
        if (!exampleFile.exists()) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Cannot find an example"),
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), exampleFile.getAbsolutePath());
            return ResponseUtils.getErrorInJson("Cannot find this example. Please choose another example.");
        }

        String fileContent;
        FileReader reader = null;
        try {
            reader = new FileReader(exampleFile);
            fileContent = ResponseUtils.readData(reader, true);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), exampleFile.getAbsolutePath());
            return ResponseUtils.getErrorInJson("Cannot find this example. Please choose another example.");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), example.fileName + " " + example.parent);
            }
        }

        resultMap.put("name", example.name);
        resultMap.put("text", fileContent);
        resultMap.put("args", example.args);
        resultMap.put("runner", example.runner);
        resultMap.put("dependencies", example.dependencies);


        array.put(resultMap);
        return array.toString();
    }

}
