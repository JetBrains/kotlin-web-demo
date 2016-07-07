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

import org.jetbrains.org.objectweb.asm.*;
import org.jetbrains.webdemo.kotlin.datastructures.ClassMethodPositions;

import java.util.HashMap;
import java.util.Map;

public class MethodsFinder {
    public static ClassMethodPositions readClassMethodPositions(byte[] classFile, String classFileName) {
        ClassReader classReader = new ClassReader(classFile);
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

        return new ClassMethodPositions(
                sourceFileName[0],
                classFileName,
                positions
        );
    }
}
