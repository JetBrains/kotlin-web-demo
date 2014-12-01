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
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.environment.EnvironmentManager;
import org.jetbrains.webdemo.environment.EnvironmentManagerForServer;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;

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

        Initializer.INITIALIZER = ServerInitializer.getInstance();
        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();

        ApplicationSettings.WEBAPP_ROOT_DIRECTORY = "kotlin.web.demo.core/resources";
        ApplicationSettings.EXAMPLES_DIRECTORY = "examples/";
        ApplicationSettings.JAVA_EXECUTE = ApplicationSettings.JAVA_HOME + File.separator + "bin" + File.separator + "java";

        createManager();

        ExamplesList.getInstance();
        HelpLoader.getInstance();
        Statistics.getInstance();
    }

    protected JetCoreEnvironment createManager() {
        myEnvironmentManager.getEnvironment();
        ServerInitializer.setEnvironmentManager(myEnvironmentManager);

        return myEnvironmentManager.getEnvironment();
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
