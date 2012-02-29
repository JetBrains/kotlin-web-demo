package org.jetbrains.demo.ukhorskaya;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 11:30 AM
 */

public class Interval {
    public final Point startPoint;
    public final Point endPoint;

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
        this.startPoint = new Point(lineNumberForElementStart, charNumberForElementStart);
        this.endPoint = new Point(lineNumberForElementEnd, charNumberForElementEnd);

    }

    public class Point {
        public final int line;
        public final int charNumber;

        private Point(int line, int charNumber) {
            this.line = line;
            this.charNumber = charNumber;
        }
    }

}


