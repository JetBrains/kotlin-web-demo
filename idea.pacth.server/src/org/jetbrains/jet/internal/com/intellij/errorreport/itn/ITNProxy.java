/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.jetbrains.jet.internal.com.intellij.errorreport.itn;

import org.jetbrains.jet.internal.com.intellij.diagnostic.errordialog.Attachment;
import org.jetbrains.jet.internal.com.intellij.errorreport.bean.ErrorBean;
import org.jetbrains.jet.internal.com.intellij.openapi.util.Pair;
import org.jetbrains.jet.internal.com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.jet.internal.com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NonNls;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stathik
 * Date: Aug 4, 2003
 * Time: 8:12:00 PM
 * To change this template use Options | File Templates.
 */
public class ITNProxy {
    @NonNls
    public static final String ENCODING = "UTF8";
    public static final String POST_DELIMITER = "&";

    @NonNls
    public static final String NEW_THREAD_URL = "http://www.intellij.net/trackerRpc/idea/createScr";

    @NonNls
    private static final String HTTP_CONTENT_LENGTH = "Content-Length";
    @NonNls
    private static final String HTTP_CONTENT_TYPE = "Content-Type";
    @NonNls
    private static final String HTTP_WWW_FORM = "application/x-www-form-urlencoded";
    @NonNls
    private static final String HTTP_POST = "POST";

    public static String postNewThread(String login, String password, ErrorBean error, String compilationTimestamp, String kotlinVersion)
            throws IOException {

        @NonNls List<Pair<String, String>> params = createParametersFor(login,
                password,
                error,
                compilationTimestamp,
                kotlinVersion);

        HttpURLConnection connection = post(new URL(NEW_THREAD_URL), join(params));
        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            return "response code " + responseCode;
        }

        String reply;

        InputStream is = new BufferedInputStream(connection.getInputStream());
        try {
            reply = readFrom(is);
        } finally {
            is.close();
        }

        if ("unauthorized".equals(reply)) {
            return reply;
        }

        if (reply.startsWith("update ")) {
            return reply;
        }

        if (reply.startsWith("message ")) {
            return reply;
        }

        return reply.trim();
    }

    private static List<Pair<String, String>> createParametersFor(String login,
                                                                  String password,
                                                                  ErrorBean error,
                                                                  String compilationTimestamp,
                                                                  String kotlinVersion) {
        @NonNls List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();

        params.add(Pair.create("protocol.version", "1"));

        params.add(Pair.create("user.login", login));
        params.add(Pair.create("user.password", password));

        params.add(Pair.create("os.name", " "));

        params.add(Pair.create("java.version", SystemProperties.getJavaVersion()));
        params.add(Pair.create("java.vm.vendor", SystemProperties.getJavaVmVendor()));

        params.add(Pair.create("protocol.version", "1"));

        params.add(Pair.create("user.login", login));
        params.add(Pair.create("user.password", password));

        params.add(Pair.create("java.version", SystemProperties.getJavaVersion()));
        params.add(Pair.create("java.vm.vendor", SystemProperties.getJavaVmVendor()));

        params.add(Pair.create("app.name", "Kotlin"));
        params.add(Pair.create("app.name.full", "Kotlin"));
        params.add(Pair.create("app.name.version", "Kotlin"));
        params.add(Pair.create("app.eap", "false"));
        params.add(Pair.create("app.build", "Kotlin-0.0"));
        params.add(Pair.create("app.version.major", kotlinVersion));
        params.add(Pair.create("app.version.minor", kotlinVersion));
        params.add(Pair.create("app.build.date", format(Calendar.getInstance())));
        params.add(Pair.create("app.build.date.release.", format(Calendar.getInstance())));
        params.add(Pair.create("app.update.channel", "update-chanel"));
        params.add(Pair.create("app.compilation.timestamp", compilationTimestamp));

        params.add(Pair.create("plugin.name", error.getPluginName()));
        params.add(Pair.create("plugin.version", kotlinVersion));

        params.add(Pair.create("last.action", error.getLastAction()));
        params.add(Pair.create("previous.exception",
                error.getPreviousException() == null ? null : Integer.toString(error.getPreviousException())));

        params.add(Pair.create("error.message", error.getMessage()));
        params.add(Pair.create("error.stacktrace", error.getStackTrace()));

        params.add(Pair.create("error.description", error.getDescription()));

        for (Attachment attachment : error.getAttachments()) {
            params.add(Pair.create("attachment.name", attachment.getName()));
            params.add(Pair.create("attachment.value", attachment.getEncodedBytes()));
        }

        return params;
    }

    private static String readFrom(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c;
        while ((c = is.read()) != -1) {
            out.write(c);
        }
        String s = out.toString();
        out.close();
        return s;
    }

    private static String format(Calendar calendar) {
        if (calendar == null) {
            return null;
        } else {
            return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        }
    }

    private static HttpURLConnection post(URL url, byte[] bytes) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setReadTimeout(10 * 1000);
        connection.setConnectTimeout(10 * 1000);
        connection.setRequestMethod(HTTP_POST);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty(HTTP_CONTENT_TYPE, String.format("%s; charset=%s", HTTP_WWW_FORM, ENCODING));
        connection.setRequestProperty(HTTP_CONTENT_LENGTH, Integer.toString(bytes.length));

        OutputStream out = new BufferedOutputStream(connection.getOutputStream());
        try {
            out.write(bytes);
            out.flush();
        } finally {
            out.close();
        }

        return connection;
    }

    private static byte[] join(List<Pair<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder buffer = new StringBuilder();

        for (Pair<String, String> param : params) {
            if (StringUtil.isEmpty(param.first))
                throw new IllegalArgumentException(param.toString());

            if (StringUtil.isNotEmpty(param.second))
                buffer.append(param.first + "=" + URLEncoder.encode(param.second, ENCODING) + POST_DELIMITER);
        }

        return buffer.toString().getBytes();
    }
}
