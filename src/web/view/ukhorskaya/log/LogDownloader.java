package web.view.ukhorskaya.log;

import web.view.ukhorskaya.ResponseUtils;

import java.io.*;
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
                FileReader fr = new FileReader(log);
                String response = ResponseUtils.readData(fr, true);
//                InputStream is = LogDownloader.class.getResourceAsStream("/clearhtml.html");
//                String html = ResponseUtils.readData(is, true);
//                html = html.replace("$RESPONSEBODY$", ResponseUtils.escapeString(response));
//                html = html.replace("\n", "<br/>");
                return response;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error");
                e.printStackTrace();
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

            responseJavaLogs.append(ResponseUtils.generateHtmlTag("h5", "JAVA MACHINE"));
            for (File file : files) {
                if (file.getName().contains(".log")) {
                    responseJavaLogs.append(ResponseUtils.generateHtmlTag("span", file.getName()));
                    responseJavaLogs.append(ResponseUtils.generateHtmlTag("a", "view", "href", "/log=" + file.getAbsolutePath() + "&view"));
                    responseJavaLogs.append(ResponseUtils.generateHtmlTag("a", "download", "href", "/log=" + file.getAbsolutePath() + "&download"));
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
            responseOtherLogs.append(ResponseUtils.generateHtmlTag("h5", "STATISTIC"));
            for (File file : files) {
                if (file.getName().contains("exceptions.log")) {
                    responseExceptionLogs.append(ResponseUtils.generateHtmlTag("span", file.getName()));
                    responseExceptionLogs.append(ResponseUtils.generateHtmlTag("a", "view", "href", "/log=" + file.getAbsolutePath() + "&view"));
                    responseExceptionLogs.append(ResponseUtils.generateHtmlTag("a", "download", "href", "/log=" + file.getAbsolutePath() + "&download"));
                    responseExceptionLogs.append(ResponseUtils.addNewLine());
                } else {
                    responseOtherLogs.append(ResponseUtils.generateHtmlTag("span", file.getName()));
                    responseOtherLogs.append(ResponseUtils.generateHtmlTag("a", "view", "href", "/log=" + file.getAbsolutePath() + "&view"));
                    responseOtherLogs.append(ResponseUtils.generateHtmlTag("a", "download", "href", "/log=" + file.getAbsolutePath() + "&download"));
                    responseOtherLogs.append(ResponseUtils.addNewLine());
                }
            }
        }
        responseOtherLogs.append(ResponseUtils.generateHtmlTag("h5", "EXCEPTIONS"));
        responseOtherLogs.append(responseExceptionLogs);
        responseOtherLogs.append(responseJavaLogs);

        return responseOtherLogs.toString();
    }
}
