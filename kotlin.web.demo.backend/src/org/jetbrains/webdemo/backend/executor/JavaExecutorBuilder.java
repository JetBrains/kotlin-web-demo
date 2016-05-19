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

package org.jetbrains.webdemo.backend.executor;

import org.jetbrains.webdemo.backend.BackendSettings;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaExecutorBuilder {
    private String mainClass;
    private boolean enableAssertions = false;
    private boolean enableSecurityManager = false;
    private Path policyFile;
    private Integer memoryLimit = null;
    private List<Path> classPath = new ArrayList<>();
    private List<String> arguments = new ArrayList<>();

    public JavaExecutorBuilder enableAssertions() {
        enableAssertions = true;
        return this;
    }

    public JavaExecutorBuilder enableSecurityManager() {
        enableSecurityManager = true;
        return this;
    }

    public JavaExecutorBuilder setPolicyFile(Path policyFile) {
        this.policyFile = policyFile;
        return this;
    }

    public JavaExecutorBuilder setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }

    public JavaExecutorBuilder setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public JavaExecutorBuilder addToClasspath(Path path) {
        classPath.add(path);
        return this;
    }

    public JavaExecutorBuilder addToClasspath(List<Path> paths) {
        classPath.addAll(paths);
        return this;
    }

    public JavaExecutorBuilder addArgument(String argument) {
        arguments.add(argument);
        return this;
    }

    public JavaExecutor build() {
        List<String> arguments = new ArrayList<>();

        arguments.add(BackendSettings.JAVA_EXECUTE);
        if (enableAssertions) {
            arguments.add("-ea");
        }

        if (memoryLimit != null) {
            arguments.add("-Xmx" + memoryLimit + "M");
        }

        if (enableSecurityManager) {
            arguments.add("-Djava.security.manager");
        }

        if (policyFile != null) {
            arguments.add("-Djava.security.policy=" + policyFile.toString());
        }

        if(!classPath.isEmpty()){
            arguments.add("-classpath");
            StringBuilder classPathString = new StringBuilder();
            String separator = "";
            for(Path classPathElement : classPath){
                classPathString.append(separator);
                classPathString.append(classPathElement);
                separator = File.pathSeparator;
            }
            arguments.add(classPathString.toString());
        }

        if (mainClass != null){
            arguments.add(mainClass);
        }

        arguments.addAll(this.arguments);

        return new JavaExecutor(arguments.toArray(new String[arguments.size()]));
    }

}
