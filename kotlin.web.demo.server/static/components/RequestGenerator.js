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
 * Date: 4/2/12
 * Time: 7:14 PM
 * To change this template use File | Settings | File Templates.
 */


var RequestGenerator = (function () {
    var sessionId = -1;

    function RequestGenerator() {
        setSessionId();

        var instance = {
        };

        return instance;
    }

    RequestGenerator.generateAjaxUrl = function (type, args) {
        var url = [location.protocol, '//', location.host, "/"].join('');
        return url + "kotlinServer?sessionId=" + sessionId + "&type=" + type + "&args=" + args;
    };

    function setSessionId() {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("getSessionId", "null"),
            context:document.body,
            type:"GET",
            dataType:"json",
            timeout:10000,
            success:getSessionIdSuccess
        });
    }

    function getSessionIdSuccess(data) {
        data = eval(data);
        if (data[0] != null && data[0] != '') {
            sessionId = data[0];
        }

        var info = "browser: " + navigator.appName + " " + navigator.appVersion;
        info += " " + "system: " + navigator.platform;

        $.ajax({
            url:RequestGenerator.generateAjaxUrl("sendUserData", "null"),
            context:document.body,
            type:"POST",
            data:{text:info},
            timeout:5000
        });
    }

    return RequestGenerator;
})();