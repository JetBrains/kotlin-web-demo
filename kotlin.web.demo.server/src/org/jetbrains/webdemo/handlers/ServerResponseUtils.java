package org.jetbrains.webdemo.handlers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ServerResponseUtils {
    private ServerResponseUtils() {
    }

    private static final List<String> URLS = new ArrayList<String>();

    static {
        URLS.add("http://unit-304.labs.intellij.net:8080");
        URLS.add("http://local.hadihariri.com:4000");
        URLS.add("http://hhariri.github.io/tests");
        URLS.add("http://hhariri.github.io");
        URLS.add("http://kotlin-demo.jetbrains.com");
        URLS.add("http://jetbrains.github.io/kotlin-web-site");
    }

    public static boolean isOriginAccepted(@NotNull HttpServletRequest request) {
        String originHeader = request.getHeader("origin");
        //TODO send origin headers
        if (originHeader == null) {
            return true;
        }
        if (URLS.contains(originHeader)) {
            return true;
        }

        String originWithoutHttp = ResponseUtils.substringAfterReturnAll(originHeader, "http://");
        return originWithoutHttp.equals(request.getHeader("host"));
    }

    public static void addHeadersToResponse(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        if (isOriginAccepted(request)) {
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.addHeader("Access-Control-Allow-Methods", "GET, POST");
            response.addHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type");
            response.addHeader("Access-Control-Allow-Credentials", "true");
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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", "unknown", "null");
        }
    }

    public static void writeResponse(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull String responseBody, int errorCode) throws IOException {
        PrintWriter writer = null;
        try {
            addHeadersToResponse(request, response);
            response.setStatus(errorCode);
            writer = response.getWriter();
            writer.write(responseBody);
        } finally {
            close(writer);
        }
    }
}
