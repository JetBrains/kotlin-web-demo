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

package org.jetbrains.webdemo.kotlin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.webdemo.kotlin.classloader.ChildFirstURLClassLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KotlinWrappersManager {
    private static Map<String, KotlinWrapper> wrappers = new HashMap<>();
    private static Log log = LogFactory.getLog(KotlinWrappersManager.class);
    private static String INITIALIZER_CLASSNAME = "org.jetbrains.webdemo.kotlin.impl.KotlinWrapperImpl";
    private static Path myWrappersDir;

    public static void init(Path wrappersDir, List<Path> javaLibraries) {
        myWrappersDir = wrappersDir;
        for (String kotlinVersion : wrappersDir.toFile().list()) {
            try {
                ClassLoader kotlinClassLoader = new ChildFirstURLClassLoader(getForKotlinWrapperClassLoaderURLs(kotlinVersion, wrappersDir),
                        Thread.currentThread().getContextClassLoader());
                KotlinWrapper kotlinWrapper = (KotlinWrapper) kotlinClassLoader.loadClass(INITIALIZER_CLASSNAME).newInstance();
                kotlinWrapper.init(javaLibraries);
                wrappers.put(kotlinVersion, kotlinWrapper);
            } catch (Exception e) {
                log.error("Can't initialize kotlin version " + kotlinVersion, e);
            }
        }
    }

    public static KotlinWrapper getKotlinWrapper(String kotlinVersion) {
        return wrappers.get(kotlinVersion);
    }

    private static URL[] getForKotlinWrapperClassLoaderURLs(String kotlinVersion, Path wrappersDir) {
        try {
            Path wrapperDir = wrappersDir.resolve(kotlinVersion);
            Path classesDir = wrapperDir.resolve("classes");
            Path jarsDir = wrapperDir.resolve("kotlin");

            List<URL> urls = new ArrayList<>();

            for (String jarFile : jarsDir.toFile().list()) {
                urls.add(jarsDir.resolve(jarFile).toUri().toURL());
            }
            urls.add(classesDir.toUri().toURL());
            return urls.toArray(new URL[urls.size()]);
        } catch (MalformedURLException e) {
            return new URL[0];
        }
    }

    public static Path getWrappersDir() {
        return myWrappersDir;
    }
}