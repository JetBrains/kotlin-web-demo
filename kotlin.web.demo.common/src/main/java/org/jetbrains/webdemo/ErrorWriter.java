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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class ErrorWriter {
    public static final Log log = LogFactory.getLog("exceptionLogger");
    public static ErrorWriter ERROR_WRITER = new ErrorWriter();

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

    private ErrorWriter() {

    }

    public static ErrorWriter getInstance() {
        return ERROR_WRITER;
    }

    public void writeException(String moreInfo) {

        log.error(moreInfo);
    }

    public void writeInfo(String message) {
        log.info(message);
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type) {
        log.error(type, e);
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String originUrl, String description) {
        log.error(description, e);
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String originUrl, Map<String, String> files) {
        log.error(type, e);
    }

    public void writeExceptionToExceptionAnalyzer(String message, String type, String originUrl, Map<String, String> files) {
        log.error(message);
    }

    public void writeExceptionToExceptionAnalyzer(String message, String stackTrace, String type, String originUrl, String description) {
        log.error(message);
    }

}
