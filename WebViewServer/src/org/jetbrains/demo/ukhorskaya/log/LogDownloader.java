package org.jetbrains.demo.ukhorskaya.log;

import com.google.common.io.Files;
import org.jetbrains.demo.ukhorskaya.*;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    public String download(String path) {
        File log = new File(path);

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
                    responseJavaLogs.append(ResponseUtils.generateTag("a", "view", "href", ResponseUtils.generateRequestStringWoQuery("downloadLog", file.getAbsolutePath() + "&view")));
                    responseJavaLogs.append(ResponseUtils.generateTag("a", "download", "href", ResponseUtils.generateRequestStringWoQuery("downloadLog", file.getAbsolutePath() + "&download")));
                    responseJavaLogs.append(ResponseUtils.addNewLine());
                }
            }
        }
        File logDir = new File("logs");
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
                    responseExceptionLogs.append(ResponseUtils.generateTag("a", "view", "href", ResponseUtils.generateRequestStringWoQuery("downloadLog", file.getAbsolutePath() + "&view")));
                    responseExceptionLogs.append(ResponseUtils.generateTag("a", "download", "href", ResponseUtils.generateRequestStringWoQuery("downloadLog", file.getAbsolutePath() + "&download")));
                    responseExceptionLogs.append(ResponseUtils.addNewLine());
                } else {
                    responseOtherLogs.append(ResponseUtils.generateTag("span", file.getName()));
                    responseOtherLogs.append(ResponseUtils.generateTag("a", "view", "href", ResponseUtils.generateRequestStringWoQuery("downloadLog", file.getAbsolutePath() + "&view")));
                    responseOtherLogs.append(ResponseUtils.generateTag("a", "download", "href", ResponseUtils.generateRequestStringWoQuery("downloadLog", file.getAbsolutePath() + "&download")));
                    responseOtherLogs.append(ResponseUtils.addNewLine());
                    //log=C:\Development\contrib\jet-contrib\WebView\logs\kotlincompiler.log&view
                    //http://localhost:8080/kotlinServer?sessionId=0&type=downloadLog&args=C:\Development\contrib\jet-contrib\WebView\logs\kotlincompiler.log&view
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
