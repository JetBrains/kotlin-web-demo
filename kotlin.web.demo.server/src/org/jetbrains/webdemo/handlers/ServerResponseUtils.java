package org.jetbrains.webdemo.handlers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

public class ServerResponseUtils {
    private ServerResponseUtils() {
    }

    private static String[] URLS_TO_ACCEPT = {
            "http://unit-304.labs.intellij.net:8080",
            "http://local.hadihariri.com:4000",
            "http://hhariri.github.io/tests"
    };

    public static void addHeadersToResponse(@NotNull HttpServletResponse response) {
        for (String url : URLS_TO_ACCEPT) {
            response.addHeader("Access-Control-Allow-Origin", url);
        }
        response.addHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
    }

    public static void close(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", "null");
        }
    }

    public static void writeResponse(@NotNull HttpServletResponse response, @NotNull String responseBody, int errorCode) throws IOException {
        PrintWriter writer = null;
        try {
            addHeadersToResponse(response);
            response.setStatus(errorCode);
            writer = response.getWriter();
            writer.write(responseBody);
        } finally {
            close(writer);
        }
    }
}
