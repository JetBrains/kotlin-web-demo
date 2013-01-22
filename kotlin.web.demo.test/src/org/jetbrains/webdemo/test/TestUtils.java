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

package org.jetbrains.webdemo.test;

import junit.framework.TestCase;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ResponseUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestUtils {
    public static final String TEST_SRC = "kotlin.web.demo.test/testData/";

    public static String getDataFromFile(String rootDirectory, String fileName) {
        StringBuilder resultData = new StringBuilder();
        String filePath = rootDirectory + fileName;

        File file = new File(filePath);
        try {
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader);
                String tmp = "";
                while ((tmp = reader.readLine()) != null) {
                    resultData.append(tmp);
                    resultData.append("\n");
                }
            }
        } catch (Throwable e) {
            System.err.println(file.getAbsolutePath());
            e.printStackTrace();
        }
        return resultData.toString();
    }

    public static String getDataFromFile(File file) throws IOException {
        StringBuilder resultData = new StringBuilder();
        if (file.exists()) {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String tmp = "";
            while ((tmp = reader.readLine()) != null) {
                resultData.append(tmp);
                resultData.append("\n");
                /*if (!fileName.endsWith(".txt")) {
                    resultData.append("<br>");
                }*/
            }
        }  else {
            System.err.println(file.getAbsolutePath());
        }
        return resultData.toString();
    }


    public static String replaceLineSeparators(String str) {
        return str.replaceAll("([\n])", System.getProperty("line.separator"));
    }

    public static String getNameByTestName(TestCase testCase) {
        String testName = testCase.getName();
        testName = ResponseUtils.substringAfter(testName, "test$");
        testName = testName.replace("$", "/");
        testName = testName.replace("_", " ");
        return testName;
    }

    public static String getActualResultForRequest(String urlWoLocalhost, @Nullable String data, @Nullable String query) throws IOException {
        String urlPath = /*HOST +*/ urlWoLocalhost.replace(" ", "%20");
        if (query != null) {
            urlPath = urlPath + "?sessionId=555&" + query;
        }
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        if (data != null) {
            urlConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
        }

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        } catch (FileNotFoundException e) {
            in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
        }

        String str;
        StringBuilder result = new StringBuilder();
        while ((str = in.readLine()) != null) {
            result.append(str);
        }
        in.close();

        return result.toString();
    }

    public static  String escape(String str) {
        str = str.replaceAll("<", "&amp;lt;");
        str = str.replaceAll(">", "&amp;gt;");
        return str;
    }

    public static String getUrlFromFileName(String fileName) {
        return "path=" + fileName;
    }
}
