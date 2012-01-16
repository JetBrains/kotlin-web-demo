package org.jetbrains.demo.ukhorskaya.errorsDescriptors;

import org.jetbrains.demo.ukhorskaya.Interval;
import org.jetbrains.jet.lang.diagnostics.Severity;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 11:27 AM
 */

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
