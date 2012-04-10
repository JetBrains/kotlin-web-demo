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

/* EVENTS:
 login
 logout
 */

var LoginModel = (function () {

    var instance;

    function LoginModel() {

        instance = {
            login: function(type) {
                login(type);
            },
            logout: function() {
               instance.onLogout(true, null);
            },
            getUserName: function() {
                getUserName();
            },
            onLogin: function(status, data) {},
            onLogout: function(status, data) {}
        };

        return instance;
    }

    function login(type) {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("authorization", type),
            context:document.body,
            success:onLoginSuccess,
            dataType:"text",
            type:"GET",
            timeout:10000,
            error:function () {
                instance.onLogin(false, null);
            }
        });
    }

    function getUserName() {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("getUserName", ""),
            context:document.body,
            success:function(data) {
                instance.onLogin(true, data);
            },
            dataType:"text",
            type:"GET",
            timeout:10000,
            error:function () {
                instance.onLogin(false, null);
            }
        });
    }

    function onLoginSuccess(data) {
        document.location.href = data;
    }

    return LoginModel;
})();