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

/*
 * @author max
 */
package org.jetbrains.jet.utils;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathUtil {
    private PathUtil() {
    }

    public static File getDefaultCompilerPath() {
        throw new UnsupportedOperationException("Cannot get default compiler path");
    }

    public static File getDefaultRuntimePath() {
//        File compilerPath = getDefaultCompilerPath();
//        if (compilerPath == null) return null;
//
//        File answer = new File(compilerPath, "lib/kotlin-runtime.jar");

//        return answer.exists() ? answer : null;
        return new File("kotlin-runtime.jar");
    }

    public static File getAltHeadersPath() {
        //        File compilerPath = new File("kotlin-jdk-headers.jar");
        //if (compilerPath == null) return null;

        //File answer = new File(compilerPath, "lib/alt");
        return new File("kotlin-jdk-headers.jar");
    }

    @NotNull
    public static String getJarPathForClass(@NotNull Class aClass) {
        try {
            String resourceRoot = PathManager.getResourceRoot(aClass, "/" + aClass.getName().replace('.', '/') + ".class");
            return new File(resourceRoot).getAbsoluteFile().getAbsolutePath();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static List<VirtualFile> getAltHeadersRoots() {
        List<VirtualFile> roots = new ArrayList<VirtualFile>();

        VirtualFile jarRoot = VirtualFileManager.getInstance().findFileByUrl("jar://" + "kotlin-jdk-headers.jar" + "!/");
        roots.add(jarRoot);

        /*File alts = getAltHeadersPath();

        if (alts != null) {
            for (File root : alts.listFiles()) {
                VirtualFile jarRoot = VirtualFileManager.getInstance().findFileByUrl("jar://" + root.getPath() + "!/");
                roots.add(jarRoot);
            }
        }*/
        return roots;
    }

    @NotNull
    public static VirtualFile jarFileToVirtualFile(@NotNull File file) {
        /*if (!file.exists() || !file.isFile()) {
            throw new IllegalStateException("file must exist and be regular to be converted to virtual file: " + file);
        }*/
        return VirtualFileManager.getInstance().findFileByUrl("jar://" + file.getPath() + "!/");
    }
}
