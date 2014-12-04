/*
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

/**
 * Created by atamas on 29.07.14.
 */


var GeneratedCodeView = (function () {

    function GeneratedCodeView(element) {
        var instance = {
            setOutput: function (data) {
                setOutput(data);
            }
        };

        function setOutput(data) {
            element.innerHTML = "";
            var generatedCode = document.createElement("p");
            if(data.type == "toggle-info") {
                generatedCode.className = "consoleViewInfo";
                generatedCode.innerHTML = safe_tags_replace(data.text);
            } else if(data.type == "info"){
                generatedCode.className = "consoleViewInfo";
                generatedCode.innerHTML = unEscapeString(data.text);
            } else if(data.type == "generatedJSCode"){
                generatedCode.className = "cm-s-default";
                generatedCode.innerHTML = safe_tags_replace(data.text);
                CodeMirror.runMode(data.text, "javascript", generatedCode);
            }
            element.appendChild(generatedCode);
        }

        return instance;
    }


    return GeneratedCodeView;
})();