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

package org.jetbrains.jet.internal.com.intellij.diagnostic.errordialog;

import org.jetbrains.jet.internal.com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import org.jetbrains.jet.internal.com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.jet.internal.com.intellij.util.ArrayUtil;
import org.jetbrains.jet.internal.com.intellij.util.Base64Converter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class Attachment {

    private static final String ERROR_MESSAGE_PATTERN = "[[[Can't get file contents: {0}]]]";

    private final String myPath;
    private final byte[] myBytes;
    private boolean myIncluded = true;
    private final String myDisplayText;

    public Attachment(String path, String content) {
        myPath = path;
        myDisplayText = content;
        myBytes = getBytes(content);
    }

    public Attachment(@NotNull VirtualFile file) {
        myPath = file.getPresentableUrl();
        myBytes = getBytes(file);
        myDisplayText = file.getFileType().isBinary() ? "File is binary" : LoadTextUtil.loadText(file).toString();
    }

    private static byte[] getBytes(VirtualFile file) {
        try {
            return file.contentsToByteArray();
        }
        catch (IOException e) {
            return getBytes(MessageFormat.format(ERROR_MESSAGE_PATTERN, e.getMessage()));
        }
    }

    private static byte[] getBytes(String content) {
        try {
            return content.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException ignored) {
            return ArrayUtil.EMPTY_BYTE_ARRAY;
        }
    }

    public String getDisplayText() {
        return myDisplayText;
    }

    public String getPath() {
        return myPath;
    }

    public String getName() {
        return myPath;
    }

    public String getEncodedBytes() {
        return Base64Converter.encode(myBytes);
    }

    public boolean isIncluded() {
        return myIncluded;
    }

    public void setIncluded(Boolean included) {
        myIncluded = included;
    }
}
