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

/**
 * Created by Semyon.Atamas on 8/12/2014.
 */
package org.jetbrains.webdemo.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class JunitExecutor {
    static List<TestRunInfo> output = new ArrayList<TestRunInfo>();
    static MethodsFinder.TestClass testClass;
    private static PrintStream standardOutput = System.out;


    public static void main(String[] args) {
        try {
            JUnitCore jUnitCore = new JUnitCore();
            jUnitCore.addListener(new MyRunListener());
            List<Class> classes = getAllClassesFromTheDir(new File(args[0]));
            for (Class cl : classes) {
                Request request = Request.aClass(cl);
                if(request.getRunner() instanceof ErrorReportingRunner) continue;
                String classFileName = cl.getName().replace('.', '/') + ".class";
                InputStream stream = cl.getClassLoader().getResourceAsStream(classFileName);
                testClass = MethodsFinder.readMethodPositions(stream, classFileName);
                jUnitCore.run(request);
            }
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleModule module = new SimpleModule();
                module.addSerializer(Throwable.class, new ThrowableSerializer());
                objectMapper.registerModule(module);
                System.setOut(standardOutput);
                System.out.print(objectMapper.writeValueAsString(output));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            System.setOut(standardOutput);
            System.out.print("[\"");
            e.printStackTrace();
            System.out.print("\"]");
        }
    }

    private static List<Class> getAllClassesFromTheDir(File directory) {
        List<Class> classes = new ArrayList<Class>();
        String prefix = "";
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                classes.addAll(getAllClassesFromTheDir(file, prefix));
            } else {
                if (file.getName().endsWith(".class")) {
                    try {
                        classes.add(Class.forName(prefix + file.getName().substring(0, file.getName().length() - ".class".length())));
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }
        return classes;
    }

    private static List<Class> getAllClassesFromTheDir(File directory, String prefix) {
        List<Class> classes = new ArrayList<Class>();
        prefix += directory.getName() + ".";
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                classes.addAll(getAllClassesFromTheDir(file, prefix));
            } else {
                if (file.getName().endsWith(".class")) {
                    try {
                        classes.add(Class.forName(prefix + file.getName().substring(0, file.getName().length() - ".class".length())));
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }
        return classes;
    }


}

class TestRunInfo {
    public String output = "";
    public String sourceFileName = "";
    public String className = "";
    public String methodName = "";
    public long executionTime = 0;
    public Throwable exception = null;
    public int methodPosition;
    public Status status = Status.OK;

    public enum Status {
        OK,
        FAIL,
        ERROR
    }
}


class MyRunListener extends RunListener {
    private long startTime;
    private PrintStream ignoreStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    });
    private ByteArrayOutputStream testOutputStream;
    private ErrorStream errorStream;
    private OutStream outStream;
    private TestRunInfo currentTestRunInfo;

    @Override
    public void testStarted(Description description) {
        currentTestRunInfo = new TestRunInfo();
        currentTestRunInfo.sourceFileName = JunitExecutor.testClass.getSourceFileName();
        currentTestRunInfo.className = description.getClassName();
        currentTestRunInfo.methodName = description.getMethodName();
        currentTestRunInfo.methodPosition = JunitExecutor.testClass.getMethodPosition(currentTestRunInfo.methodName);

        JunitExecutor.output.add(currentTestRunInfo);
        testOutputStream = new ByteArrayOutputStream();
        errorStream = new ErrorStream(testOutputStream);
        outStream = new OutStream(testOutputStream);
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errorStream));
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testFailure(Failure failure) {
        Throwable exception = failure.getException();
        currentTestRunInfo.exception = exception;

        if (exception instanceof AssertionError) {
            currentTestRunInfo.status = TestRunInfo.Status.FAIL;
        } else {
            currentTestRunInfo.status = TestRunInfo.Status.ERROR;
        }
    }

    @Override
    public void testFinished(Description description) {
        System.out.flush();
        System.err.flush();

        TestRunInfo testRunInfo = JunitExecutor.output.get(JunitExecutor.output.size() - 1);
        testRunInfo.executionTime = System.currentTimeMillis() - startTime;
        testRunInfo.output = testOutputStream.toString()
                .replaceAll("</errStream><errStream>", "")
                .replaceAll("</outStream><outStream>", "");

        System.setOut(ignoreStream);
    }
}