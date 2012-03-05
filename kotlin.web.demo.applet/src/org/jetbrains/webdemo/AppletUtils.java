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

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/29/11
 * Time: 2:00 PM
 */
public class AppletUtils {
    public static String getContentFormServer(String request, @Nullable String data) {
        String urlPath = "http://localhost/editor?sessionId=555&allExamples=true";
        StringBuilder result = new StringBuilder();
        URL url = null;
        try {
            url = new URL(urlPath);

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return "";
            }

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
            while ((str = in.readLine()) != null) {
                result.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return "";
        }
        return result.toString();
    }


}
