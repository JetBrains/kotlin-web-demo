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

public class CompletionVariant {
    private final String text;
    private final String displayText;
    private final String tail;
    private final String icon;

    public CompletionVariant(String text, String displayText, String tail, String icon) {
        this.text = text;
        this.displayText = displayText;
        this.tail = tail;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getTail() {
        return tail;
    }

    public String getIcon() {
        return icon;
    }
}
