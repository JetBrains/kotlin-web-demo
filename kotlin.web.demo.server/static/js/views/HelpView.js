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
 * Created by atamas on 04.08.14.
 */

var HelpView = (function () {

    function HelpView(helpModel) {
        var model = helpModel;
        model.loadAllHelpElements();

        var instance = {
            setPosition: function (pos) {
                element.style.left = pos.left + 2 + "px";
                element.style.top = pos.top + 15 + "px";
            },
            update: function (name) {
                var text = model.getHelpElement(name);
                if (text != null) {
                    setText(text);
                } else {
                    instance.hide();
                }
            },
            hide: function () {
                element.style.display = "none"
            }
        };

        var element = document.createElement("div");
        element.className = "words-help";
        document.body.appendChild(element);

        var textElement = document.createElement("div");
        textElement.className = "text";
        element.appendChild(textElement);

        function setText(text) {
            element.style.display = "block";
            if (checkDataForNull(text)) {
                textElement.innerHTML = text;
            }
        }

        return instance;
    }


    return HelpView;
})();