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

var LoginView = (function () {
    function LoginView(loginModel) {
        var model = loginModel;
        var isLoggedIn = false;

        var confirmDialog = new ConfirmDialog();

        var instance = {
            setUserName: function (name) {
                if (name != "[\"null\"]") setUserName(eval(name)[0]);
            },
            isLoggedIn: function () {
                return isLoggedIn;
            },
            logout: function () {
                isLoggedIn = false;
                $("#login").css("display", "block");
                $("#userName").html("").css("display", "none");
            }
        };

        $("#login-with-twitter").click(function () {
            login("twitter");
        });
        $("#login-with-twitter-colored").click(function () {
            login("twitter");
        });

        $("#login-with-facebook").click(function () {
            login("facebook");
        });
        $("#login-with-facebook-colored").click(function () {
            login("facebook");
        });

        $("#login-with-google").click(function () {
            login("google");
        });
        $("#login-with-google-colored").click(function () {
            login("google");
        });

        document.getElementById("logout").onclick = function () {
            model.logout();
        };

        model.getUserName();

        function login(param) {
            confirmDialog.open(function (param) {
                return function () {
                    model.login(param);
                };
            }(param));
        }

        function setUserName(userName) {
            if (userName != "") {
                $("#login").css("display", "none");
                $("#userName").css("display", "inline-block");

                isLoggedIn = true;
                userName = decodeURI(userName);
                userName = replaceAll(userName, "\\+", " ");

                $("#userNameTitle").text("Welcome, " + userName);

            }
        }

        return instance;
    }

    return LoginView;
})();