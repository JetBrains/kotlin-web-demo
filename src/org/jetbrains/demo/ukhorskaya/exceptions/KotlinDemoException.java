package org.jetbrains.demo.ukhorskaya.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 2/2/12
 * Time: 5:06 PM
 */
public class KotlinDemoException extends Exception {
   private String stackTrace;
    
    public KotlinDemoException(String message) {
        super(message);
    }

    public KotlinDemoException(String message, String stackTrace) {
        super(message);
        this.stackTrace = stackTrace;
    }

    public KotlinDemoException() {
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        s.print(stackTrace);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            s.println(this);
            s.print(stackTrace);
        }
        
    }
}
