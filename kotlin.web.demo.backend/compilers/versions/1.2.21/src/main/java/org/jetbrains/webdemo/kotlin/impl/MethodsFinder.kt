/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.impl

import jdk.internal.org.objectweb.asm.*
import org.jetbrains.webdemo.kotlin.datastructures.ClassMethodPositions
import java.util.*

object MethodsFinder {
    fun readClassMethodPositions(classFile: ByteArray, classFileName: String): ClassMethodPositions {
        val classReader = ClassReader(classFile)
        val positions = HashMap<String, Int>()

        val sourceFileName = arrayOfNulls<String>(1)

        classReader.accept(
                object : ClassVisitor(Opcodes.ASM5) {
                    override fun visitSource(source: String, debug: String) {
                        sourceFileName[0] = source
                    }

                    override fun visitMethod(
                            access: Int, name: String, desc: String, signature: String, exceptions: Array<String>
                    ): MethodVisitor {
                        return object : MethodVisitor(Opcodes.ASM5) {
                            override fun visitLineNumber(line: Int, start: Label) {
                                var position: Int? = positions[name]
                                if (position == null || position > line) {
                                    position = line
                                }
                                positions.put(name, position)
                            }
                        }
                    }
                },
                0
        )

        return ClassMethodPositions(
                sourceFileName[0],
                classFileName,
                positions
        )
    }
}
