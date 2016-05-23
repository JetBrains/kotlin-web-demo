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

package org.jetbrains.webdemo.kotlin.impl;

import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;

import java.nio.file.Path;

public class WrapperSettings {
    public static String KOTLIN_VERSION = "1.0.1-2";
    public static Path WRAPPER_DIV = KotlinWrappersManager.WRAPPERS_DIR.resolve(KOTLIN_VERSION);
    public static Path KOTLIN_JARS_FOLDER = WRAPPER_DIV.resolve("kotlin");
    public static Path JS_LIB_ROOT = WRAPPER_DIV.resolve("js");

}
