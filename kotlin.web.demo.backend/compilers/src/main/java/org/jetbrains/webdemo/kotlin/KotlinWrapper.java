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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.kotlin.datastructures.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface KotlinWrapper {
    void init(List<Path> javaLibraries, String kotlinVersion, String build);

    String translateJavaToKotlin(String javaCode);

    TranslationResult compileKotlinToJS(Map<String, String> files, String[] args);

    Map<String, List<ErrorDescriptor>> getErrors(Map<String, String> files, boolean isJs);

    List<CompletionVariant> getCompletionVariants(Map<String, String> projectFiles, String filename, int line, int ch, boolean isJs);

    CompilationResult compileCorrectFiles(Map<String, String> projectFiles, @Nullable String filename);

    List<Path> getKotlinRuntimeLibraries();

    MethodPositions getMethodPositions(Map<String, byte[]> classFiles);

    Path getKotlinRuntimeJar();

    Path getWrapperFolder();

    String getWrapperVersion();
}
