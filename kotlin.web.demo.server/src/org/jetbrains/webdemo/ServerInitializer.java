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

package org.jetbrains.webdemo;

import org.jetbrains.webdemo.environment.EnvironmentManager;
import org.jetbrains.webdemo.environment.EnvironmentManagerForServer;

public class ServerInitializer extends Initializer {
    private static ServerInitializer initializer = new ServerInitializer();

    public static ServerInitializer getInstance() {
        return initializer;
    }

    private ServerInitializer() {
    }

    private static EnvironmentManager environmentManager = new EnvironmentManagerForServer();

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
}


