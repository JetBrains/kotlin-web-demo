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

package org.jetbrains.webdemo.kotlin.datastructures;

public class TextPosition {
    private int line;
    private int ch;

    public TextPosition(int line, int ch) {
        this.line = line;
        this.ch = ch;
    }

    public int getLine() {
        return line;
    }

    public int getCh() {
        return ch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextPosition that = (TextPosition) o;

        if (line != that.line) return false;
        return ch == that.ch;

    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + ch;
        return result;
    }
}
