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

package org.jetbrains.webdemo.backend;

import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.backend.enviroment.EnvironmentManager;
import org.jetbrains.webdemo.backend.enviroment.EnvironmentManagerForServer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerInitializer extends Initializer {
    private static ServerInitializer initializer = new ServerInitializer();
    private static EnvironmentManager environmentManager = new EnvironmentManagerForServer();

    private ServerInitializer() {
    }

    public static ServerInitializer getInstance() {
        return initializer;
    }

    public boolean initJavaCoreEnvironment() {
        try {
            environmentManager.getEnvironment();
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("Impossible to init jetCoreEnvironment", e);
            return false;
        }

        return true;
    }

    @Override
    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }

    public static void setEnvironmentManager(EnvironmentManager environmentManager) {
        ServerInitializer.environmentManager = environmentManager;
    }

    public void initializeExecutorsPolicyFile() throws IOException {
        Path templateFilePath = Paths.get(BackendSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy.template");
        String templateFileContent = new String(Files.readAllBytes(templateFilePath));
        String policyFileContent = templateFileContent.replaceAll("@WEBAPPS_ROOT@", BackendSettings.WEBAPP_ROOT_DIRECTORY.replaceAll("\\\\", "/"));
        try (PrintWriter policyFile = new PrintWriter(BackendSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy")) {
            policyFile.write(policyFileContent);
        }
    }
}


