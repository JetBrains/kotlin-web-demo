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

/**
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/30/12
 * Time: 3:37 PM
 */


var ProblemsView = function (element, /*Nullable*/ tabs) {
    var console = document.createElement("div");
    console.className = "result-view";
    element.append(console);

    var instance = {
        addMessages: function (data) {
            addMessagesToProblemsView(data);
        },
        clear: function () {
            console.innerHTML = "";
        }
    };

    function addMessagesToProblemsView(data) {
        console.innerHTML = "";
        if (tabs != null) {
            tabs.tabs("option", "active", 0);
        }
        var i = 0;

        function processError(i, f) {
            if (data[i] == undefined) {
                return;
            }

            var title = unEscapeString(data[i].titleName);
            var start = eval('(' + data[i].x + ')');
            var severity = data[i].severity;

            var problem = createElementForProblemsView(severity, start, title);
            console.appendChild(problem);
            i++;

            setTimeout(function (i) {
                return function () {
                    f(i, processError);
                }
            }(i), 10);
        }

        processError(i, processError);
    }

    function makeCodeReference(lineNo, charNo) {
        var codeReference = document.createElement("a");
        codeReference.href = "#";
        var id = "error_" + lineNo + "_" + charNo;
        codeReference.id = id;
        codeReference.innerHTML = (lineNo + 1) + "," + (charNo + 1);
        $(document).on('click', "#" + id, function () {
            editor.setCursor(lineNo, charNo);
            editor.focus();
        });

        return codeReference
    }

    function createElementForProblemsView(severity, start, title) {
        var p = document.createElement("p");
        var img = document.createElement("div");
        if (severity == 'WARNING') {
            img.className = "problemsViewWarningImg";
            if (title.indexOf("is never used") > 0) {
                p.className = "problemsViewWarningNeverUsed";
            } else {
                p.className = "problemsViewWarning";
            }

        } else if (severity == 'STACKTRACE') {
            p.className = "problemsViewStacktrace";
        } else {
            img.className = "problemsViewErrorImg";
            p.className = "problemsViewError";
        }
        p.appendChild(img);
        var titleDiv = document.createElement("span");
        if (start == null) {
            titleDiv.innerHTML = " " + unEscapeString(title);
        } else {
            var span = document.createElement("span");
            span.innerHTML = "(";
            p.appendChild(span);

            p.appendChild(makeCodeReference(start.line, start.ch));

            titleDiv.innerHTML = "): " + unEscapeString(title);
        }
        p.appendChild(titleDiv);
        return p;
    }


    return instance;
};