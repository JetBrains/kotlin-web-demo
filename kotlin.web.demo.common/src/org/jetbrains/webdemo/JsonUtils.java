/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Semyon.Atamas on 2/27/2015.
 */
public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();
    public static String toJson(Object object){
        try{
            return objectMapper.writeValueAsString(object);
        }  catch (IOException e) {
            /*Unreachable*/
            ErrorWriter.getInstance().writeExceptionToExceptionAnalyzer(e, "Json serialization", "", "");
        }
        return "";
    }
}
