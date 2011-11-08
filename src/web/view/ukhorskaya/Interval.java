package web.view.ukhorskaya;

import com.intellij.openapi.editor.Document;

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
        int lineNumberForElementStart = currentDocument.getLineNumber(start);
        int lineNumberForElementEnd = currentDocument.getLineNumber(end);
        int charNumberForElementStart = start - currentDocument.getLineStartOffset(lineNumberForElementStart);
        int charNumberForElementEnd = end - currentDocument.getLineStartOffset(lineNumberForElementStart);
        if ((start == end) && (lineNumberForElementStart == lineNumberForElementEnd)) {
            charNumberForElementStart--;
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


