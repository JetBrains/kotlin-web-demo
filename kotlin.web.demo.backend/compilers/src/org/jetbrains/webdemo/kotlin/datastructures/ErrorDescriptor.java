/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.datastructures;


import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public class ErrorDescriptor {
    @NotNull
    private final TextInterval interval;
    @NotNull
    private final String message;
    @NotNull
    private final Severity severity;
    @NotNull
    private String className = null;

    public ErrorDescriptor(TextInterval interval, String message, Severity severity, String className) {
        this.interval = interval;
        this.message = message;
        this.severity = severity;
        if (className != null) {
            this.className = className;
        } else {
            this.className = severity.name();
        }
    }

    public TextInterval getInterval() {
        return interval;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorDescriptor that = (ErrorDescriptor) o;

        if (!interval.equals(that.interval)) return false;
        if (!message.equals(that.message)) return false;
        if (severity != that.severity) return false;
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        int result = interval.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result + className.hashCode();
        return result;
    }
}
