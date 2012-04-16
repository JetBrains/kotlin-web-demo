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


var CanvasPopup = (function () {


    function CanvasPopup(element) {

        var instance = {
            show:function () {
                element.html("");
                element.append("<canvas width=\"" + $("#popupForCanvas").dialog("option", "width")
                    + "\" height=\"" + ($("#popupForCanvas").dialog("option", "height") - 50) + "\" id=\"mycanvas\"></canvas>");
                element.dialog("open");
            },
            hide:function () {
                element.html("");
                //element.dialog("close");
                //WARN: if in the page there is more intevals - they will be stopped.
                window.clearAllIntervals();
            }
        };

        return instance;
    }

    return CanvasPopup;
})();