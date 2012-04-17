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

var CompletionDecorator = (function () {

    function CompletionDecorator() {
        var currentCompleter = null;
        var instance = {
            getCompletion:function (configurationType, programText, cursorLine, cursorCh) {
                if (currentCompleter != null) {
                    currentCompleter.getCompletion(configurationType, programText, cursorLine, cursorCh);
                }
            },
            onLoadCompletion:function (data) {

            },
            setCompleter:function (completer) {
                if (currentCompleter != null) {
                    currentCompleter.onLoadCompletion = null;
                    currentCompleter.onFail = null;
                }
                currentCompleter = completer;
                currentCompleter.onLoadCompletion = function (data) {
                    instance.onLoadCompletion(data);
                };
                currentCompleter.onFail = function (exception) {
                    instance.onFail(exception);
                };
            },
            onFail:function (message) {
            }
        };

        return instance;
    }

    return CompletionDecorator;
})();
