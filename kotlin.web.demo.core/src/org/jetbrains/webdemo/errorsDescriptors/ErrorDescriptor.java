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

package org.jetbrains.webdemo.errorsDescriptors;

import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.webdemo.Interval;

public class ErrorDescriptor {

    private final Interval interval;
    private final String message;
    private final Severity severity;

    private String className = null;

    public ErrorDescriptor(Interval interval, String message, Severity severity, String className) {
        this.interval = interval;
        this.message = message;
        this.severity = severity;
        this.className = className;
    }


    public ErrorDescriptor(Interval interval, String message, Severity severity) {
        this.interval = interval;
        this.message = message;
        this.severity = severity;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getClassName() {
        if (className == null) {
            className = severity.name();
        }
        return className;
    }
}
