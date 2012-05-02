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

import com.intellij.openapi.Disposable;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;
import org.jetbrains.jet.plugin.JetFileType;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:49 PM
 */
public class InitializerApplet extends Initializer {
    private static InitializerApplet initializer = new InitializerApplet();

    public static InitializerApplet getInstance() {
        return initializer;
    }

    private InitializerApplet() {
    }

    private static JetCoreEnvironment environment;

    public JetCoreEnvironment getEnvironment() {
        if (environment != null) {
            return environment;
        }
        return null;
    }

    public boolean initJavaCoreEnvironment() {
        if (environment == null) {

            Disposable root = new Disposable() {
                @Override
                public void dispose() {
                }
            };
            environment = new JetCoreEnvironment(root, CompilerDependencies.compilerDependenciesForProduction(CompilerSpecialMode.REGULAR));

//            environment.addToClasspath(new File(InitializerApplet.class.getResource("rt.jar")));
//            environment.addToClasspath(new File("kotlin-runtime.jar"));
//            environment.addToClasspath(new File("kotlin-compiler.jar"));

            environment.registerFileType(JetFileType.INSTANCE, "kt");
            environment.registerFileType(JetFileType.INSTANCE, "kts");
            environment.registerFileType(JetFileType.INSTANCE, "ktm");
            environment.registerFileType(JetFileType.INSTANCE, "jet");
            environment.registerParserDefinition(new JetParserDefinition());


            return true;
            //return setJavaCoreEnvironment();
        }
        return false;
    }
}


