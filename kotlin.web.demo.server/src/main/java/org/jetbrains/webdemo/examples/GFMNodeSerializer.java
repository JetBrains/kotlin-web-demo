/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package org.jetbrains.webdemo.examples;

import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.VerbatimNode;

/**
 * @author Alexander Prendota on 12/25/17
 */

public class GFMNodeSerializer extends ToHtmlSerializer {
    public GFMNodeSerializer() {
        super(new LinkRenderer());
    }

    public void visit(VerbatimNode node) {
        String codeMirrorType;
        switch (node.getType()) {
            case "kotlin":
                codeMirrorType = "text/x-kotlin";
                break;
            case "java":
                codeMirrorType = "text/x-java";
                break;
            default:
                codeMirrorType = "text/x-kotlin";
                break;
        }
        if (!codeMirrorType.equals("")) {
            printer.print("<pre><code data-lang=\"" + codeMirrorType + "\">");
        } else {
            printer.print("<pre><code>");
        }
        printer.printEncoded(node.getText());
        printer.print("</code></pre>");
    }
}