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

package org.jetbrains.webdemo.backend;

import com.intellij.openapi.editor.Document;

public class Interval {
    public final Point start;
    public final Point end;

    public Interval(int start, int end, Document currentDocument) {

        /*PsiFile file = diagnostic.getPsiFile();
        Document document = file.getViewProvider().getDocument();
        TextRange firstRange = diagnostic.getTextRanges().iterator().next();
        int offset = firstRange.getStartOffset();
        if (document != null) {
            int lineNumber = document.getLineNumber(offset);
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int column = offset - lineStartOffset;

            return "(" + (lineNumber + 1) + "," + (column + 1) + ")";
        }
        return "(offset: " + offset + " line unknown)";*/


        int lineNumberForElementStart = currentDocument.getLineNumber(start);
        int lineNumberForElementEnd = currentDocument.getLineNumber(end);
        int charNumberForElementStart = start - currentDocument.getLineStartOffset(lineNumberForElementStart);
        int charNumberForElementEnd = end - currentDocument.getLineStartOffset(lineNumberForElementStart);
        if ((start == end) && (lineNumberForElementStart == lineNumberForElementEnd)) {
            charNumberForElementStart--;
            if (charNumberForElementStart < 0) {
                charNumberForElementStart++;
                charNumberForElementEnd++;
            }
        }
        this.start = new Point(lineNumberForElementStart, charNumberForElementStart);
        this.end = new Point(lineNumberForElementEnd, charNumberForElementEnd);

    }

    public Interval(Point start, Point end){
        this.start = start;
        this.end = end;
    }

    public class Point {
        public int line;
        public int ch;

        private Point(int line,int ch) {
            this.line = line;
            this.ch = ch;
        }
    }

}


