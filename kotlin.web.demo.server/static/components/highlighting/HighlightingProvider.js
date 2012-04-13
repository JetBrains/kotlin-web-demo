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
 * To change this template use File | Settings | File Templates.
 */

var HighlightingProvider = (function () {

    var instance;


    var highlightingFromClient = new HighlightingFromClient();
    var highlightingFromServer = new HighlightingFromServer();

    var currentHighlighter = highlightingFromServer;

    function HighlightingProvider() {

        highlightingFromClient.onHighlight = function (data, callback) {
            instance.onHighlight(data);
            callback(data);
        };
        highlightingFromClient.onFail = function (exception) {
            instance.onFail(exception);
        };

        highlightingFromServer.onHighlight = function (data, callback) {
            instance.onHighlight(data);
            callback(data);
        };
        highlightingFromServer.onFail = function (exception) {
            instance.onFail(exception);
        };


        instance = {
            getHighlighting:function (configurationType, programText, callback) {
                currentHighlighter.getHighlighting(configurationType, programText, callback);
            },
            onHighlight:function (data) {

            },
            setConfiguration:function (configuration) {
                if (configuration.mode == Configuration.mode.CLIENT) {
                    currentHighlighter = highlightingFromClient;
                } else if (configuration.mode == Configuration.mode.SERVER) {
                    currentHighlighter = highlightingFromServer;
                } else {
                    currentHighlighter = highlightingFromServer;
                }
            },
            onFail:function (message) {
            },
            checkIfThereAreErrors:function (data) {
                var i = 0;
                while (typeof data[i] != "undefined") {
                    var severity = data[i].severity;
                    if (severity == "ERROR") {
                        return true;
                    }
                    i++;
                }
                return false;
            }
        };

        return instance;
    }

    return HighlightingProvider;
})();