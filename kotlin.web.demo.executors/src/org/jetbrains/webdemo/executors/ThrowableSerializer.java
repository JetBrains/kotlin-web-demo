package org.jetbrains.webdemo.executors;/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by Semyon.Atamas on 11/27/2014.
 */

public class ThrowableSerializer extends JsonSerializer<Throwable> {

    @Override
    public void serialize(Throwable throwable, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("message", throwable.getMessage() != null ? throwable.getMessage() : "");
        jsonGenerator.writeStringField("fullName", throwable.getClass().getName());
        jsonGenerator.writeObjectField("stackTrace", throwable.getStackTrace());
        jsonGenerator.writeObjectField("cause", throwable.getCause() != throwable ? throwable.getCause() : null);
        jsonGenerator.writeEndObject();
    }
}
