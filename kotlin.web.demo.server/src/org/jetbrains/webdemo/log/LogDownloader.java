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

package org.jetbrains.webdemo.log;

import org.jetbrains.jet.internal.com.google.common.io.Files;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.Statistics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/21/11
 * Time: 4:37 PM
 */

public class LogDownloader {
    public String download(String name) {
        File log = new File(ApplicationSettings.LOGS_DIRECTORY + File.separator + name);

        if (log.exists()) {
            try {
                return Files.toString(log, Charset.forName("utf-8"));
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), log.getAbsolutePath());
            }
        }
        return "";
    }

    public String getFilesLinks() {
        StringBuilder responseExceptionLogs = new StringBuilder();
        StringBuilder responseOtherLogs = new StringBuilder();
        StringBuilder responseJavaLogs = new StringBuilder();
        File rootDir = new File("");
        if ((rootDir.exists()) && (rootDir.isDirectory())) {
            File[] files = rootDir.listFiles();
            if (files == null) {
                return "";
            }

            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    int result = (Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()));
                    if (result == 0) {
                        return result;
                    } else {
                        return -result;
                    }
                }
            });

            responseJavaLogs.append(ResponseUtils.generateTag("h5", "JAVA MACHINE"));
            for (File file : files) {
                if (file.getName().contains(".log")) {
                    responseJavaLogs.append(ResponseUtils.generateTag("span", file.getName()));
                    responseJavaLogs.append(ResponseUtils.generateTag("a", "view", "href", ResponseUtils.generateRequestString("downloadLog", file.getName() + "&view")));
                    responseJavaLogs.append(ResponseUtils.generateTag("a", "download", "href", ResponseUtils.generateRequestString("downloadLog", file.getName() + "&download")));
                    responseJavaLogs.append(ResponseUtils.addNewLine());
                }
            }
        }
        File logDir = new File(ApplicationSettings.LOGS_DIRECTORY);
        if ((logDir.exists()) && (logDir.isDirectory())) {
            File[] files = logDir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    int result = (Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()));
                    if (result == 0) {
                        return result;
                    } else {
                        return -result;
                    }
                }
            });
            responseOtherLogs.append(ResponseUtils.generateTag("h5", "STATISTIC"));
            for (File file : files) {
                if (file.getName().contains("exceptions.log")) {
                    responseExceptionLogs.append(ResponseUtils.generateTag("span", file.getName()));
                    responseExceptionLogs.append(ResponseUtils.generateTag("a", "view", "href", ResponseUtils.generateRequestString("downloadLog", file.getName() + "&view")));
                    responseExceptionLogs.append(ResponseUtils.generateTag("a", "download", "href", ResponseUtils.generateRequestString("downloadLog", file.getName() + "&download")));
                    responseExceptionLogs.append(ResponseUtils.addNewLine());
                } else {
                    responseOtherLogs.append(ResponseUtils.generateTag("span", file.getName()));
                    responseOtherLogs.append(ResponseUtils.generateTag("a", "view", "href", ResponseUtils.generateRequestString("downloadLog", file.getName() + "&view")));
                    responseOtherLogs.append(ResponseUtils.generateTag("a", "download", "href", ResponseUtils.generateRequestString("downloadLog", file.getName() + "&download")));
                    responseOtherLogs.append(ResponseUtils.addNewLine());
                }
            }
        }
        responseOtherLogs.append(ResponseUtils.generateTag("h5", "EXCEPTIONS"));
        responseOtherLogs.append(responseExceptionLogs);
        responseOtherLogs.append(responseJavaLogs);

        return responseOtherLogs.toString();
    }

    public String getSortedExceptions(String from, String to) {
        return Statistics.getInstance().getSortedExceptions(from, to);
    }
}
