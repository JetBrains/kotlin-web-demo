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
 * Created by Semyon.Atamas on 12/4/2014.
 */

var DifferenceDialogView = (function () {
    function DifferenceDialogView() {
        var instance = {
            open:function(difference){
                leftElement.innerHTML = difference.expected;
                rightElement.innerHTML = difference.actual;
                $(dialogElement).dialog("open");
            }
        };
        var dialogElement = document.createElement("div");
        document.body.appendChild(dialogElement);
        dialogElement.title = "Comparison failure";

        var leftElement = document.createElement("div");
        leftElement.className = "difference-expected";
        dialogElement.appendChild(leftElement);

        var rightElement = document.createElement("div");
        rightElement.className = "difference-actual";
        dialogElement.appendChild(rightElement);

        $(dialogElement).dialog({
            autoOpen: false,
            modal: true
        });
        return instance;
    }

    return DifferenceDialogView;
})();