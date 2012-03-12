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

package org.jetbrains.webdemo.responseHelpers;

import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/20/11
 * Time: 5:19 PM
 */
public class JsConverter {
    private final SessionInfo info;

    public JsConverter(SessionInfo info) {
        this.info = info;
    }

    public String getResult(String code, String arguments) {
        JSONArray result = new JSONArray();
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put("text", WebDemoTranslatorFacade.translateStringWithCallToMain(code, arguments));
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), code + "\n" + arguments);
            map.put("exception", e.getMessage());
        }
        result.put(map);
        return result.toString();
    }
}
