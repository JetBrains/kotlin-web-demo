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

package org.jetbrains.webdemo.test;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import junit.framework.TestCase;
import org.jetbrains.kotlin.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.backend.Initializer;
import org.jetbrains.webdemo.backend.enviroment.EnvironmentManager;
import org.jetbrains.webdemo.backend.responseHelpers.JavaToKotlinConverter;
import org.jetbrains.webdemo.examples.ExamplesLoader;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseTest extends TestCase {

    protected SessionInfo sessionInfo = new SessionInfo("test");
    protected EnvironmentManager myEnvironmentManager = new EnvironmentManager();

    public BaseTest(String name) {
        super(name);
    }
    public BaseTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        Path currentRelativePath = Paths.get("");
        String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();

        Initializer.getInstance();
        ErrorWriter.ERROR_WRITER = ErrorWriter.getInstance();

        BackendSettings.WEBAPP_ROOT_DIRECTORY = currentAbsolutePath + File.separator + "kotlin.web.demo.test" + File.separator + "resources";
        BackendSettings.CLASS_PATH = currentAbsolutePath + File.separator + "out" + File.separator + "production";
        ApplicationSettings.EXAMPLES_DIRECTORY = "examples";
        BackendSettings.JAVA_EXECUTE = BackendSettings.JAVA_HOME + File.separator + "bin" + File.separator + "java";
        BackendSettings.LIBS_DIR = currentAbsolutePath + File.separator + "lib";
        JavaToKotlinConverter.init();

        createManager();
        initializePolicyFile();

        ExamplesLoader.loadAllExamples();
        HelpLoader.getInstance();
    }

    protected JetCoreEnvironment createManager() {
        myEnvironmentManager.getEnvironment();
        Initializer.setEnvironmentManager(myEnvironmentManager);

        return myEnvironmentManager.getEnvironment();
    }

    protected void initializePolicyFile() throws IOException {
        Path templateFilePath = Paths.get(BackendSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy.template");
        String templateFileContent = new String(Files.readAllBytes(templateFilePath));
        String policyFileContent = templateFileContent.replaceAll("@CLASS_PATH@", BackendSettings.CLASS_PATH.replaceAll("\\\\", "/"));
        policyFileContent = policyFileContent.replaceAll("@LIBS_DIR@", BackendSettings.LIBS_DIR.replaceAll("\\\\", "/"));
        try (PrintWriter policyFile = new PrintWriter(BackendSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy")) {
            policyFile.write(policyFileContent);
        }
    }

    protected Project getProject() {
        return myEnvironmentManager.getEnvironment().getProject();
    }

    @Override
    public void tearDown() throws Exception {
        Disposer.dispose(myEnvironmentManager.getDisposable());
        myEnvironmentManager = null;
        sessionInfo = null;
        super.tearDown();
    }
}
