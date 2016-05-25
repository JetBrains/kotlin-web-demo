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

import junit.framework.TestCase;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.CommonSettings;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesLoader;
import org.jetbrains.webdemo.kotlin.KotlinWrapper;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseTest extends TestCase {
    protected static KotlinWrapper kotlinWrapper = null;
    private static boolean initialized = false;

    public BaseTest(String name) {
        super(name);
    }

    public BaseTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!initialized) {
            Path currentAbsolutePath = TestUtils.getApplicationFolder();
            CommonSettings.WEBAPP_ROOT_DIRECTORY = currentAbsolutePath + File.separator + "kotlin.web.demo.test" + File.separator + "resources";
            BackendSettings.CLASS_PATH = currentAbsolutePath + File.separator + "out" + File.separator + "production";
            BackendSettings.JAVA_EXECUTE = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            BackendSettings.LIBS_DIR = currentAbsolutePath.resolve("lib").toString();

            KotlinWrappersManager.init(currentAbsolutePath.resolve("kotlin.web.demo.backend").resolve("compilers").resolve("versions"));
            kotlinWrapper = KotlinWrappersManager.getKotlinWrapper("1.0.1-2");

            initializePolicyFile();
            if(ExamplesFolder.ROOT_FOLDER == null) {
                ApplicationSettings.LOAD_TEST_VERSION_OF_EXAMPLES = true;
                ApplicationSettings.EXAMPLES_DIRECTORY = currentAbsolutePath.resolve("examples").toString();
                ExamplesLoader.loadAllExamples();
            }
            initialized = true;
        }
    }

//    protected KotlinCoreEnvironment createManager() {
//        myEnvironmentManager.getEnvironment();
//        Initializer.setEnvironmentManager(myEnvironmentManager);
//
//        return myEnvironmentManager.getEnvironment();
//    }

    protected void initializePolicyFile() throws IOException {
        Path kotlinLibsDir = kotlinWrapper.getKotlinCompilerJar().getParent();
        Path templateFilePath = Paths.get(CommonSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy.template");
        String templateFileContent = new String(Files.readAllBytes(templateFilePath));
        String policyFileContent = templateFileContent.replaceAll("@CLASS_PATH@", BackendSettings.CLASS_PATH.replaceAll("\\\\", "/"));
        policyFileContent = policyFileContent.replaceAll("@LIBS_DIR@", BackendSettings.LIBS_DIR.replaceAll("\\\\", "/"));
        policyFileContent = policyFileContent.replaceAll("@KOTLIN_LIBS@", kotlinLibsDir.toString().replaceAll("\\\\", "/"));
        try (PrintWriter policyFile = new PrintWriter(CommonSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "executors.policy")) {
            policyFile.write(policyFileContent);
        }
    }

//    protected Project getProject() {
//        return myEnvironmentManager.getEnvironment().getProject();
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        Disposer.dispose(myEnvironmentManager.getDisposable());
//        myEnvironmentManager = null;
//        sessionInfo = null;
//        super.tearDown();
//    }
}
