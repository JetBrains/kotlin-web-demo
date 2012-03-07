package org.jetbrains.web.demo.test;/*
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

import junit.framework.TestCase;
import org.jetbrains.jet.compiler.CompileEnvironment;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;

/**
 * @author Natalia.Ukhorskaya
 */

public class BaseTest extends TestCase {

    protected SessionInfo sessionInfo = new SessionInfo("test");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
        Initializer.INITIALIZER = ServerInitializer.getInstance();

//        ServerSettings.JAVA_HOME = "c:\\Program Files\\Java\\jdk1.6.0_30\\";
        ServerSettings.JAVA_EXECUTE = "c:\\Program Files\\Java\\jdk1.6.0_30\\bin\\java.exe";

        ServerSettings.EXAMPLES_ROOT = "examples/";
        boolean initEnvironment = ServerInitializer.getInstance().initJavaCoreEnvironment();
        assertEquals("Initialisation of java core environment failed, server didn't start.",
                true, initEnvironment);
        ExamplesList.getInstance();
        HelpLoader.getInstance();
        Statistics.getInstance();
    }
}
