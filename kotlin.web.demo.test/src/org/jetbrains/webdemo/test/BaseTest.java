package org.jetbrains.webdemo.test;
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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import junit.framework.TestCase;
import org.jetbrains.kotlin.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.environment.EnvironmentManager;
import org.jetbrains.webdemo.environment.EnvironmentManagerForServer;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.responseHelpers.JavaToKotlinConverter;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseTest extends TestCase {

    protected SessionInfo sessionInfo = new SessionInfo("test");
    protected EnvironmentManager myEnvironmentManager = new EnvironmentManagerForServer();

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

        Initializer.INITIALIZER = ServerInitializer.getInstance();
        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();

        ApplicationSettings.WEBAPP_ROOT_DIRECTORY = currentAbsolutePath + File.separator + "kotlin.web.demo.test" + File.separator + "resources";
        ApplicationSettings.CLASS_PATH = currentAbsolutePath + File.separator + "out" + File.separator + "production";
        ApplicationSettings.EXAMPLES_DIRECTORY = "examples";
        ApplicationSettings.JAVA_EXECUTE = ApplicationSettings.JAVA_HOME + File.separator + "bin" + File.separator + "java";
        ApplicationSettings.LIBS_DIR = currentAbsolutePath + File.separator + "lib";
        JavaToKotlinConverter.init();

        createManager();
        initializePolicyFile();

        ExamplesList.getInstance();
        HelpLoader.getInstance();
        Statistics.getInstance();
    }

    protected JetCoreEnvironment createManager() {
        myEnvironmentManager.getEnvironment();
        ServerInitializer.setEnvironmentManager(myEnvironmentManager);

        return myEnvironmentManager.getEnvironment();
    }

    protected void initializePolicyFile() throws IOException {
        Path templateFilePath = Paths.get(ApplicationSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy.template");
        String templateFileContent = new String(Files.readAllBytes(templateFilePath));
        String policyFileContent = templateFileContent.replaceAll("@CLASS_PATH@", ApplicationSettings.CLASS_PATH.replaceAll("\\\\", "/"));
        policyFileContent = policyFileContent.replaceAll("@LIBS_DIR@", ApplicationSettings.LIBS_DIR.replaceAll("\\\\", "/"));
        try (PrintWriter policyFile = new PrintWriter(ApplicationSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy")) {
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
