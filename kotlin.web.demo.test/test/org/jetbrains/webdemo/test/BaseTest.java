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
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class BaseTest extends TestCase {
    protected KotlinWrapper kotlinWrapper = null;
    private static boolean initialized = false;
    @Rule
    public TestName name = new TestName();

    public BaseTest(String kotlinVersion, String name) {
        super(name);
        kotlinWrapper = KotlinWrappersManager.INSTANCE.getKotlinWrapper(kotlinVersion);
    }

    public BaseTest(String kotlinVersion) {
        kotlinWrapper = KotlinWrappersManager.INSTANCE.getKotlinWrapper(kotlinVersion);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        init();
    }

    public static void init() throws IOException, URISyntaxException {
        if (!initialized) {
            Path currentAbsolutePath = TestUtils.getApplicationFolder();
            CommonSettings.WEBAPP_ROOT_DIRECTORY = currentAbsolutePath + File.separator + "kotlin.web.demo.test" + File.separator + "resources";
            BackendSettings.CLASS_PATH = currentAbsolutePath + File.separator + "out" + File.separator + "production";
            BackendSettings.JAVA_EXECUTE = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            BackendSettings.EXECUTORS_LIBS_DIR = currentAbsolutePath.resolve("kotlin.web.demo.executors")
                    .resolve("build").resolve("libs").toString();

            Path wrappersDir = currentAbsolutePath.resolve("versions");
            Path junitLib = Paths.get(BackendSettings.EXECUTORS_LIBS_DIR, "junit-4.12.jar");
            KotlinWrappersManager.INSTANCE.init(wrappersDir, Collections.singletonList(junitLib), Paths.get("build", "classes", "java", "main"));
            KotlinWrappersManager.INSTANCE.init(wrappersDir, Collections.singletonList(junitLib), Paths.get("build", "classes", "kotlin", "main"));

            initializePolicyFiles();
            if (ExamplesFolder.ROOT_FOLDER == null) {
                ApplicationSettings.LOAD_TEST_VERSION_OF_EXAMPLES = true;
                ApplicationSettings.EXAMPLES_DIRECTORY = currentAbsolutePath.resolve("kotlin.web.demo.server")
                        .resolve("examples").toString();
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

    protected static void initializePolicyFiles() throws IOException, URISyntaxException {
        for (KotlinWrapper wrapper : KotlinWrappersManager.INSTANCE.getAllWrappers()) {
            Path templateFilePath = Paths.get(KotlinWrappersManager.class.getResource("/executors.policy.template").toURI());
            String templateFileContent = new String(Files.readAllBytes(templateFilePath));
            String policyFileContent = templateFileContent;
            policyFileContent = policyFileContent.replaceAll("@LIBS_DIR@", BackendSettings.EXECUTORS_LIBS_DIR.replaceAll("\\\\", "/"));
            policyFileContent = policyFileContent.replaceAll("@KOTLIN_RUNTIME@", wrapper.getKotlinRuntimeJar().toUri().toString().replace("file:///", ""));
            try (PrintWriter policyFile = new PrintWriter(wrapper.getWrapperFolder().resolve("executors.policy").toFile())) {
                policyFile.write(policyFileContent);
            }
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

    @Parameterized.Parameters(name = "kotlin-{0}")
    public static Collection<Object[]> data() throws IOException, URISyntaxException {
        init();
        List<Object[]> parameters = new ArrayList<>();
        Collection<String> versions = KotlinWrappersManager.INSTANCE.getKotlinVersions();
        for (String version : versions) {
            parameters.add(new Object[]{version});
        }
        return parameters;
    }
}
