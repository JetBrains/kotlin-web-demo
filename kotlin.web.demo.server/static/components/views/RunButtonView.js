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
 * To change this template use File | Settings | File Templates.
 */


var RunButtonView = (function () {

    var model = new RunModel();

    function RunButtonView() {

        var instance = {
            setVisible: function(){
                $("#run").css({opacity:1});
            },
            buttonClick: function() {
                runButtonClick();
            }
        };

        if (navigator.appVersion.indexOf("Mac") != -1) {
            var title = $("#run").attr("title").replace("F9", "R");
            $("#run").attr("title", title);
        }

        $("#run").click(function () {
            runButtonClick();
        });

        return instance;
    }

    function runButtonClick() {
        $("#run").css({opacity:0.5});
        model.run();
    }

    return RunButtonView;
})();