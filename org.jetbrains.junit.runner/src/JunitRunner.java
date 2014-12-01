/*
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

/**
 * Created by Semyon.Atamas on 8/12/2014.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class JunitRunner {
    static List<TestRunInfo> output = new ArrayList<TestRunInfo>();
    private static PrintStream standardOutput = System.out;

    public static void main(String[] args) {
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new MyRunListener());
        for (String className : args) {
            try {
                jUnitCore.run(Class.forName(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            System.setOut(standardOutput);
            System.out.print(new ObjectMapper().writeValueAsString(output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

class TestRunInfo {
    public String output = "";
    public String className = "";
    public String methodName = "";
    public long executionTime = 0;
    public ExceptionDescriptor exception = null;
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
        currentTestRunInfo.className = description.getClassName();
        currentTestRunInfo.methodName = description.getMethodName();
        JunitRunner.output.add(currentTestRunInfo);
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
        currentTestRunInfo.exception = new ExceptionDescriptor();
        currentTestRunInfo.exception.message = exception.getMessage();
        currentTestRunInfo.exception.stackTrace = exception.getStackTrace();
        currentTestRunInfo.exception.fullName = exception.getClass().getName();

        if (exception instanceof AssertionError) {
            currentTestRunInfo.status = TestRunInfo.Status.FAIL;
        } else {
            currentTestRunInfo.status = TestRunInfo.Status.ERROR;
        }
    }

    @Override
    public void testFinished(Description description) {
        JunitRunner.output.get(JunitRunner.output.size() - 1).executionTime = System.currentTimeMillis() - startTime;
        System.out.flush();
        System.err.flush();
        JunitRunner.output.get(JunitRunner.output.size() - 1).output = testOutputStream.toString();
        System.setOut(ignoreStream);
    }
}