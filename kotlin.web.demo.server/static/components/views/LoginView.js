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

var LoginView = (function () {

    var model = new LoginModel();
    var isLoggedIn = false;

    var confirmDialog = new ConfirmDialog();

    function LoginView() {
        var instance = {
            processUserName: function (status, name) {
                if (status && name != "[\"null\"]") setUserName(eval(name)[0]);
            },
            isLoggedIn: function() {
                return isLoggedIn;
            },
            processLogout: function (status, data) {
                if (status) {
                    isLoggedIn = false;
                    $("#login").css("display", "block");
                    $("#userName").html("");
                    $("#userName").css("display", "none");
                }
            }
        };

        $("a > img[src*='twitter']").click(function () {
            login("twitter");
        });
        $("a > img[src*='facebook']").click(function () {
            login("facebook");
        });
        $("a > img[src*='google']").click(function () {
            login("google");
        });

        $("#logout").click(function (e) {
            model.logout();
        });

        model.getUserName();

        return instance;
    }

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
            $("#userName").css("display", "block");
            isLoggedIn = true;
            userName = decodeURI(userName);
            userName = replaceAll(userName, "\\+", " ");

            $("#userName").html("<div id='userNameTitle'><span>Welcome, " + userName + "</span><img src='/static/images/toogleShortcutsOpen.png' id='userNameImg'/></div>");
            document.getElementById("userNameTitle").onclick = function (e) {
                userNameClick(e);
            };
        }
    }

    var isLogoutShown = false;

    function userNameClick(e) {
        if (!isLogoutShown) {
            $("#headerlinks").bind("mouseleave", function () {
                var timeout = setTimeout(function () {
                    close();
                }, 100);
                $("#logout").bind("mouseover", function () {

                    clearTimeout(timeout);
                    $("#logout").bind("mouseleave", function () {
                        timeout = setTimeout(function () {
                            close();
                        }, 500);
                    });
                });

            });

            isLogoutShown = true;
            var div = document.createElement("div");
            div.id = "logout";
            div.innerHTML = "Logout";
            div.style.position = "absolute";

            var element = document.getElementById("userNameTitle");
            if (element == null) {
                return;
            }

            var left = element.offsetLeft;
            var top = element.offsetTop;
            for (var parent = element.offsetParent; parent; parent = parent.offsetParent) {
                left += parent.offsetLeft - parent.scrollLeft;
                top += parent.offsetTop - parent.scrollTop
            }

            div.style.left = left + 240 - 42 + "px";
            div.style.top = top + 27 - 3 + "px";
            div.onclick = function () {
                close();
                model.logout();
            };
            document.body.appendChild(div);
        } else {
            close();
        }


        function close() {
            isLogoutShown = false;
            var el = document.getElementById("logout");
            if (el != null) {
                el.parentNode.removeChild(document.getElementById("logout"));
            }
        }
    }

    return LoginView;
})();