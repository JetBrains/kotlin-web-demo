package web.view.ukhorskaya.log;

import web.view.ukhorskaya.ResponseUtils;

import java.io.*;

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
        StringBuilder response = new StringBuilder();
        File logDir = new File("logs");
        if ((logDir.exists()) && (logDir.isDirectory())) {
            File[] files = logDir.listFiles();
            for (File file : files) {
                response.append(ResponseUtils.generateHtmlTag("a", file.getName(), "href", "/log=" + file.getAbsolutePath()));
                response.append(ResponseUtils.addNewLine());
            }
        }

        return response.toString();
    }
}
