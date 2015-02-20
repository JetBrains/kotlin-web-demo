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

package org.jetbrains.webdemo.backend.responseHelpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.kotlin.j2k.J2kPackage;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.backend.Initializer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSessionInfo;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JavaToKotlinConverter {
    private final BackendSessionInfo info;
    /**
     * Main method of java2kotlin converter. This method will change Extensions
     * during the call, and it's not thread-safe to reinitialize them after it.
     * That's why this method is loaded with a different class loader wia reflection/
     */
    private static Method translateToKotlin;

    public static void init(){
        try {
            URL[] urls = {
                    new File(BackendSettings.KOTLIN_LIBS_DIR + File.separator + "j2k.jar").toURI().toURL(),
                    new File(BackendSettings.KOTLIN_LIBS_DIR + File.separator + "kotlin-compiler.jar").toURI().toURL(),
                    new File(BackendSettings.KOTLIN_LIBS_DIR + File.separator + "kotlin-runtime.jar").toURI().toURL()
            };
            URLClassLoader classLoader = new URLClassLoader(urls);
            translateToKotlin = classLoader.loadClass(J2kPackage.class.getName()).getMethod("translateToKotlin", String.class);
            assert(translateToKotlin.invoke(null, "class A").equals("class A"));
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("Couldn't initialize Java2Kotlin converter", e);
        }
    }
    
    public JavaToKotlinConverter(BackendSessionInfo info) {
        this.info = info;
    }

    public String getResult(String code) {
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode jsonObject = result.addObject();
        try {
            String resultFormConverter;
            try {
                resultFormConverter = (String) translateToKotlin.invoke(null, code);
            } catch (Exception e) {
                return ResponseUtils.getErrorInJson("EXCEPTION: " + e.getMessage());
            }
            if (resultFormConverter.isEmpty()) {
                return ResponseUtils.getErrorInJson("EXCEPTION: generated code is empty.");
            }
            jsonObject.put("text", resultFormConverter);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    BackendSessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN.name(), info.getOriginUrl(), code);
            return ResponseUtils.getErrorInJson(e.getMessage());
        }

        return result.toString();
    }

}
