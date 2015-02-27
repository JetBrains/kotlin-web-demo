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

import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.errorreport.bean.ErrorBean;
import com.intellij.errorreport.itn.ITNProxy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ErrorWriter {
    public static final Logger LOG_FOR_EXCEPTIONS = Logger.getLogger("exceptionLogger");
    public static final Logger LOG_FOR_INFO = Logger.getLogger("infoLogger");
    public static ErrorWriter ERROR_WRITER = new ErrorWriter();

    public static void writeErrorToConsole(String message) {
        System.err.println(message);
    }

    public static void writeExceptionToConsole(String message, Throwable e) {
        System.err.println(message);
        e.printStackTrace();
    }

    public static void writeExceptionToConsole(Throwable e) {
        e.printStackTrace();
    }

    public static void writeInfoToConsole(String message) {
        System.out.println(message);
    }

    public static String getExceptionForLog(String typeOfRequest, Throwable throwable, String originUrl, String moreinfo) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n<error>");
        builder.append("\n<version>");
        builder.append(CommonSettings.KOTLIN_VERSION);
        builder.append("</version>");
        builder.append("\n<type>");
        builder.append(ResponseUtils.escapeString(typeOfRequest));
        builder.append("</type>");
        builder.append("\n<message>");
        builder.append(ResponseUtils.escapeString(throwable.getMessage()));
        builder.append("</message>");
        builder.append("\n<stack>");
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        builder.append(ResponseUtils.escapeString(stringWriter.toString()));
        builder.append("\n</stack>");
        builder.append("\n<moreinfo>");
        builder.append("\n");
        builder.append("Origin url: ");
        builder.append(ResponseUtils.escapeString(originUrl));
        builder.append("\n").append(ResponseUtils.escapeString(moreinfo));
        builder.append("\n</moreinfo>");
        builder.append("\n</error>");
        return builder.toString();
    }

    public static List<String> parseException(String text) {
        ArrayList<String> list = new ArrayList<String>();
        //0
        String str = ResponseUtils.substringBetween(text, "<version>", "</version>");
        list.add(str);
        //1
        str = ResponseUtils.substringBetween(text, "<type>", "</type>");
        list.add(str);
        //2
        str = ResponseUtils.substringBetween(text, "<message>", "</message>");
        list.add(str);
        //3
        str = ResponseUtils.substringBetween(text, "<stack>", "</stack>");
        list.add(str);
        //4
        str = ResponseUtils.substringBetween(text, "<moreinfo>", "</moreinfo>");
        list.add(str);

        return list;
    }

    public static String getExceptionForLog(String typeOfRequest, String originUrl, String message, String stackTrace, String moreinfo) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n<error>");
        builder.append("\n<version>");
        builder.append(CommonSettings.KOTLIN_VERSION);
        builder.append("</version>");
        builder.append("\n<type>");
        builder.append(ResponseUtils.escapeString(typeOfRequest));
        builder.append("</type>");
        builder.append("\n<message>");
        builder.append(ResponseUtils.escapeString(message));
        builder.append("</message>");
        builder.append("\n<stack>");
        builder.append(ResponseUtils.escapeString(stackTrace));
        builder.append("\n</stack>");
        builder.append("\n<moreinfo>");
        builder.append("\n");
        builder.append("Origin url: ");
        builder.append(ResponseUtils.escapeString(originUrl));
        builder.append("\n").append(ResponseUtils.escapeString(moreinfo));
        builder.append("\n</moreinfo>");
        builder.append("\n</error>");
        return builder.toString();
    }

    public static String getExceptionForLog(String typeOfRequest, String message, String originUrl, String moreinfo) {
        return getExceptionForLog(typeOfRequest, message, message, originUrl, moreinfo);
    }

    public static String getInfoForLog(String typeOfRequest, String userId, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("type=").append(typeOfRequest);
        builder.append(" ");
        builder.append("userId=");
        builder.append(String.valueOf(userId));
        builder.append(" ip=0 ");
        builder.append("message=").append(message);
        return builder.toString();
    }

    public static String getInfoForLogWoIp(String typeOfRequest, String userId, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("type=").append(typeOfRequest);
        builder.append(" ");
        builder.append("userId=");
        builder.append(String.valueOf(userId));
        builder.append(" ");
        builder.append("message=").append(message);
        return builder.toString();
    }

    private ErrorWriter() {

    }

    public static ErrorWriter getInstance() {
        return ERROR_WRITER;
    }

    public void writeException(String moreInfo) {

        LOG_FOR_EXCEPTIONS.error(moreInfo);
    }

    public void writeInfo(String message) {
        LOG_FOR_INFO.info(message);
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String originUrl, String description) {
        ErrorBean bean = new ErrorBean(e, type);
        bean.setPluginName("Kotlin Web Demo Beta");
        bean.setAttachments(Collections.singletonList(new Attachment("Example.kt", description)));
        if (!CommonSettings.IS_TEST_VERSION) {
            sendViaITNProxy(bean);
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, e, originUrl, description));
        } else {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, e, originUrl, description));
        }
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String originUrl, Map<String, String> files) {
        ErrorBean bean = new ErrorBean(e, type);
        bean.setPluginName("Kotlin Web Demo Beta");
        List<Attachment> attachments = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (String fileName : files.keySet()) {
            attachments.add(new Attachment(fileName, files.get(fileName)));
            description.append(fileName).append(":\n");
            description.append(files.get(fileName));
        }
        bean.setAttachments(attachments);
        if (!CommonSettings.IS_TEST_VERSION) {
            sendViaITNProxy(bean);
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, e, originUrl, description.toString()));
        } else {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, e, originUrl, description.toString()));
        }
    }

    public void writeExceptionToExceptionAnalyzer(String message, String type, String originUrl, Map<String, String> files) {
        ErrorBean bean = new ErrorBean(message, "", type);
        bean.setPluginName("Kotlin Web Demo Beta");
        List<Attachment> attachments = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (String fileName : files.keySet()) {
            attachments.add(new Attachment(fileName, files.get(fileName)));
            description.append(fileName).append(":\n");
            description.append(files.get(fileName));
        }
        bean.setAttachments(attachments);
        if (!CommonSettings.IS_TEST_VERSION) {
            sendViaITNProxy(bean);
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, message, originUrl, description.toString()));
        } else {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, message, originUrl, description.toString()));
        }
    }

    public void writeExceptionToExceptionAnalyzer(String message, String stackTrace, String type, String originUrl, String description) {
        ErrorBean bean = new ErrorBean(message, stackTrace, type);
        bean.setAttachments(Collections.singletonList(new Attachment("Example.kt", description)));
        bean.setPluginName("Kotlin Web Demo Beta");

        if (!CommonSettings.IS_TEST_VERSION) {
            sendViaITNProxy(bean);
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, message, stackTrace, description));
        } else {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog(type, message, stackTrace, description));
        }
    }

    private void sendViaITNProxy(ErrorBean error) {
        String login = "idea_anonymous";
        String password = "guest";
        try {
            String result = ITNProxy.postNewThread(login, password, error, String.valueOf(System.currentTimeMillis()), CommonSettings.KOTLIN_VERSION);
            if ("unauthorized".equals(result) || result.startsWith("update ") || result.startsWith("message ")) {
                LOG_FOR_EXCEPTIONS.error(getExceptionForLog("SEND_TO_EA", result, "", ""));
                LOG_FOR_EXCEPTIONS.error(getExceptionForLog(error.getLastAction(), error.getMessage(), "", error.getDescription()));
            } else {
                LOG_FOR_INFO.info("Submitted to Exception Analyzer: " + result);
            }
        } catch (IOException e1) {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog("SEND_TO_EXCEPTION_ANALYZER", e1, "", login));
        }

    }
}
