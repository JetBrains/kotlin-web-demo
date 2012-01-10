package web.view.ukhorskaya.exceptions;

import web.view.ukhorskaya.ResponseUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/24/11
 * Time: 9:54 AM
 */

public class KotlinCoreException extends RuntimeException {
    private final Throwable e;

    public KotlinCoreException(Throwable e) {
        this.e = e;
    }

    @Override
    public String getMessage() {
        return e.getMessage();
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }

    public String getStackTraceString() {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString().replaceAll("\t", "");
        stackTrace = stackTrace.replaceAll(System.getProperty("line.separator"), ResponseUtils.addNewLine());
        return stackTrace;
    }
}
