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

package org.jetbrains.webdemo.executors;

import org.jetbrains.org.objectweb.asm.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Semyon.Atamas on 12/26/2014.
 */
public class MethodsFinder {
    public static TestClass readMethodPositions(InputStream stream, String classFileName) throws IOException {
        try {
            ClassReader classReader = new ClassReader(new BufferedInputStream(stream));
            final Map<String, Integer> positions = new HashMap<String, Integer>();

            final String[] sourceFileName = new String[1];

            classReader.accept(
                    new ClassVisitor(Opcodes.ASM5) {
                        @Override
                        public void visitSource(String source, String debug) {
                            sourceFileName[0] = source;
                        }

                        @Override
                        public MethodVisitor visitMethod(
                                int access, final String name, String desc, String signature, String[] exceptions
                        ) {
                            return new MethodVisitor(Opcodes.ASM5) {
                                @Override
                                public void visitLineNumber(int line, Label start) {
                                    Integer position = positions.get(name);
                                    if (position == null || position > line) {
                                        position = line;
                                    }
                                    positions.put(name, position);
                                }
                            };
                        }
                    },
                    0
            );

            return new TestClass(
                    classFileName,
                    sourceFileName[0],
                    positions
            );
        } finally {
            stream.close();
        }
    }

    public static class TestClass {
        private final String classFileName;
        private final String sourceFileName;
        private final Map<String, Integer> methodPositions;

        public TestClass(String classFileName, String sourceFileName, Map<String, Integer> methodPositions) {
            this.classFileName = classFileName;
            this.sourceFileName = sourceFileName;
            this.methodPositions = new HashMap<String, Integer>(methodPositions);
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public String getClassFileName() {
            return classFileName;
        }

        // Nullable
        public Integer getMethodPosition(String methodName) {
            return methodPositions.get(methodName);
        }
    }
}
