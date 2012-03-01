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
import org.jetbrains.webdemo.server.ServerSettings;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/5/11
 * Time: 12:30 PM
 */

public class ErrorWriterOnServer extends ErrorWriter {
    public static final Logger LOG_FOR_EXCEPTIONS = Logger.getLogger("exceptionLogger");
    public static final Logger LOG_FOR_INFO = Logger.getLogger("infoLogger");

    private static final ErrorWriterOnServer writer = new ErrorWriterOnServer();

    private ErrorWriterOnServer() {

    }

    @Override
    public void writeException(String moreInfo) {

        LOG_FOR_EXCEPTIONS.error(moreInfo);
    }

    @Override
    public void writeInfo(String message) {
        LOG_FOR_INFO.info(message);
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String description) {
        ErrorBean bean = new ErrorBean(e, type);
//        bean.addProgramText(description);
        bean.setPluginName("Kotlin Web Demo");
        bean.setAttachments(Collections.singletonList(new Attachment("Example.kt", description)));
        if (ServerSettings.IS_TEST_VERSION.equals("false")) {
            sendViaITNProxy(bean);
            LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(type, e, description));
        } else {
            LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(type, e, description));
        }
    }


    public void writeExceptionToExceptionAnalyzer(String message, String stackTrace, String type, String description) {
        ErrorBean bean = new ErrorBean(message, stackTrace, type);
//        bean.addProgramText(description);
        bean.setAttachments(Collections.singletonList(new Attachment("Example.kt", description)));
        bean.setPluginName("Kotlin Web Demo");

        if (ServerSettings.IS_TEST_VERSION.equals("false")) {
            sendViaITNProxy(bean);
            LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(type, message, stackTrace, description));
        } else {
            LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(type, message, stackTrace, description));
        }
    }

    private void sendViaITNProxy(ErrorBean error) {
//        String login = "Natalia.Ukhorskaya";
//        String password = "pqow02";
        String login = "idea_anonymous";
        String password = "guest";
        try {
            String result = ITNProxy.postNewThread(login, password, error, String.valueOf(System.currentTimeMillis()), ServerSettings.KOTLIN_VERSION);
            System.out.println(result);
            if ("unauthorized".equals(result) || result.startsWith("update ") || result.startsWith("message ")) {
                LOG_FOR_EXCEPTIONS.error(getExceptionForLog("SEND_TO_EA", result, ""));
                LOG_FOR_EXCEPTIONS.error(getExceptionForLog(error.getLastAction(), error.getMessage(), error.getDescription()));
            } else {
                LOG_FOR_INFO.info("Submitted to Exception Analyzer: " + result);
            }
        } catch (IOException e1) {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog("SEND_TO_EXCEPTION_ANALYZER", e1, login));
        }

    }

    public static ErrorWriterOnServer getInstance() {
        return writer;
    }
}
