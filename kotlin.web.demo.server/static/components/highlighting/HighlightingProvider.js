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
 * Date: 3/29/12
 * Time: 1:56 PM
 */

var HighlighterDecorator = (function () {

    function HighlighterDecorator() {
        var currentHighlighter = null;

        var instance = {
            getHighlighting:function (configurationType, programText, callback) {
                if (currentHighlighter != null) {
                    currentHighlighter.getHighlighting(configurationType, programText, callback);
                }
            },
            onHighlight:function (data) {

            },
            setHighlighter:function (highlighter) {
                if (currentHighlighter != null) {
                    currentHighlighter.onHighlight = null;
                    currentHighlighter.onFail = null;
                }
                currentHighlighter = highlighter;
                currentHighlighter.onHighlight = function (data, callback) {
                    instance.onHighlight(data);
                    callback(data);
                };
                currentHighlighter.onFail = function (exception) {
                    instance.onFail(exception);
                };
            },
            onFail:function (message) {
            }
        };

        return instance;
    }

    return HighlighterDecorator;
})();


/*var arrayOfHighlighters = [];
 arrayOfHighlighters.push(Configuration.mode.CLIENT.highlighter);
 arrayOfHighlighters.push(Configuration.mode.SERVER.highlighter);

 var i = 0;
 for (i = 0; i < arrayOfHighlighters.length; ++i) {
 arrayOfHighlighters[i].onHighlight = function (data, callback) {
 instance.onHighlight(data);
 callback(data);
 };
 arrayOfHighlighters[i].onFail = function (exception) {
 instance.onFail(exception);
 };
 }*/