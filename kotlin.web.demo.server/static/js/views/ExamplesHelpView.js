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
 * Created by Semyon.Atamas on 8/11/2014.
 */


var ExamplesHelpView = (function () {
    function ExamplesHelpView(element) {
        var instance = {
            showHelp: function (data) {
                if(data != null || data == ""){
                    element.parent().css("display", "block");
                    element.html(data);
                } else{
                    element.html("There are no description for this example");
                }
            },

            hide: function () {
                element.parent().css("display", "none");
            }
        }
        return instance;
    }

    return ExamplesHelpView;
})();