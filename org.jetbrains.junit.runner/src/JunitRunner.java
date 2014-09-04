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

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;


public class JunitRunner {

    public static void main(String[] args) {
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new MyRunListener());
        for (String className : args) {
            long startTime = System.currentTimeMillis();
            try {
                System.out.println("@" + className + " started@");
                System.err.println("@" + className + " started@");
                jUnitCore.run(Class.forName(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("@time: " + executionTime + "@");
                System.out.println("@" + className + " finished@");
                System.err.println("@" + className + " finished@");
            }
        }
    }


}


class MyRunListener extends RunListener {
    long startTime;

    @Override
    public void testStarted(Description description) {
        System.out.println("@" + description.getDisplayName() + " started@");
        System.err.println("@" + description.getDisplayName() + " started@");
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testFailure(Failure failure) {
        if (failure.getException() instanceof AssertionError) {
            System.out.println(failure.getMessage());
            System.out.println("@" + failure.getTestHeader() + " failed@");
        } else {
            failure.getException().printStackTrace();
            System.out.println("@" + failure.getTestHeader() + " error@");
        }
    }

    @Override
    public void testFinished(Description description) {
        long executionTime = System.currentTimeMillis() - startTime;
        System.out.println("@time: " + executionTime + "@");
        System.out.println("@" + description.getDisplayName() + " finished@");
        System.err.println("@" + description.getDisplayName() + " finished@");
    }
}