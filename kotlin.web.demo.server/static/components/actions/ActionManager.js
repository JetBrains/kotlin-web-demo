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

var Shortcut = (function () {
    function Shortcut(shortcutName, functionToCheckIfShortcutPressed) {
        var name = shortcutName;
        var isShortcutPressed = functionToCheckIfShortcutPressed;

        var instance = {
            isPressed:function (e) {
                if (isShortcutPressed != null && isShortcutPressed(e)) {
                    if (e.preventDefault) e.preventDefault();
                    else e.returnValue = false;
                    return true;
                }
                return false;
            },
            getName:function () {
                return name;
            }
        };
        return instance;
    }

    return Shortcut;
})();

var ActionManager = (function () {

    var NEVER_PRESSED_SHORTCUT = new Shortcut("", function (e) {
       return false;
    });

    function ActionManager() {
        var macActionShortcutMap = [];
        var winActionShortcutMap = [];
        var defaultActionShortcutMap = [];

        var currentActionShortcutMap = setShortcuts();

        function setShortcuts() {
            if (navigator.appVersion.indexOf("Mac") != -1) {
                return macActionShortcutMap;
            } else {
                return defaultActionShortcutMap;
            }
        }

        var instance = {
            getShortcutByName:function (name) {
                for (var i = 0; i < currentActionShortcutMap.length; i++) {
                    if (currentActionShortcutMap[i].name == name) {
                        return  currentActionShortcutMap[i].shortcut;
                    }
                }
                return NEVER_PRESSED_SHORTCUT;
            },
            registerAction:function (actionName, defaultShortcut, macShortcut, winShortcut) {
                if (macShortcut != undefined) {
                    macActionShortcutMap.push({name:actionName, shortcut:macShortcut});
                } else {
                    macActionShortcutMap.push({name:actionName, shortcut:defaultShortcut});
                }

                if (winShortcut != undefined) {
                    winActionShortcutMap.push({name:actionName, shortcut:winShortcut});
                } else {
                    winActionShortcutMap.push({name:actionName, shortcut:defaultShortcut});
                }

                defaultActionShortcutMap.push({name:actionName, shortcut:defaultShortcut});
            }
        };

        return instance;
    }


    return ActionManager;
})();
