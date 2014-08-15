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

package org.jetbrains.webdemo;

import org.jetbrains.webdemo.environment.EnvironmentManager;
import org.jetbrains.webdemo.environment.EnvironmentManagerForServer;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.translator.WebDemoConfigServer;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;


public class Env
{
    public static EnvironmentManager environmentManager = new EnvironmentManagerForServer();
    public static ServerInitializer initializer = new ServerInitializer();

    Env() {

        environmentManager.createEnvironment();
        ServerInitializer.setEnvironmentManager(environmentManager);
        Initializer.reinitializeJavaEnvironment();
        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
        try {
            if (ServerInitializer.getInstance().initJavaCoreEnvironment()) {
                ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                WebDemoTranslatorFacade.LOAD_JS_LIBRARY_CONFIG = new WebDemoConfigServer(Initializer.INITIALIZER.getEnvironment().getProject());
                ExamplesList.getInstance();
                HelpLoader.getInstance();
                Statistics.getInstance();
                //MySqlConnector.getInstance();
            } else {
                ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
            }
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }
    }
}
