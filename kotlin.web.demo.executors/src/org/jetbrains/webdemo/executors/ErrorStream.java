package org.jetbrains.webdemo.executors;/*
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Semyon.Atamas on 11/20/2014.
 */
class ErrorStream extends OutputStream {
    private OutputStream outputStream;

    ErrorStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write("<span class=\"error-output\">".getBytes());
        outputStream.write(b);
        outputStream.write("</span>".getBytes());
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write("<span class=\"error-output\">".getBytes());
        outputStream.write(b);
        outputStream.write("</span>".getBytes());
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        outputStream.write("<span class=\"error-output\">".getBytes());
        outputStream.write(b, offset, length);
        outputStream.write("</span>".getBytes());
    }
}
