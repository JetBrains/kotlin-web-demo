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

package org.jetbrains.webdemo.executors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import junit.framework.ComparisonFailure;

import java.io.IOException;

public class JunitFrameworkComparisonFailureSerializer extends JsonSerializer<ComparisonFailure> {
    @Override
    public void serialize(ComparisonFailure exception, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("message", exception.getMessage());
        jsonGenerator.writeStringField("expected", exception.getExpected());
        jsonGenerator.writeStringField("actual", exception.getActual());
        jsonGenerator.writeStringField("fullName", exception.getClass().getName());
        jsonGenerator.writeObjectField("stackTrace", exception.getStackTrace());
        jsonGenerator.writeObjectField("cause", exception.getCause() != exception ? exception.getCause() : null);
        jsonGenerator.writeEndObject();
    }
}

