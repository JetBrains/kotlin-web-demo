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
 * Time: 12:27 PM
 */

var ConfirmDialog = (function () {

    function ConfirmDialog() {

        var instance = {
            open:function (f) {
                showConfirmDialog(f);
            },
            close:function () {
                closeConfirmDialog();
            }
        };

        $("#confirmDialog").dialog({
            modal:"true",
            width:500,
            height:120,
            autoOpen:false
        });

        return instance;
    }


    ConfirmDialog.isEditorContentChanged = function () {
        return false;
    };
    ConfirmDialog.isLoggedIn = function () {
        return false;
    };
    ConfirmDialog.saveProgram = function () {

    };

    function showConfirmDialog(fun) {
        if (ConfirmDialog.isEditorContentChanged()) {
            $("#confirmDialog").dialog({
                buttons:[
                    { text:"Save changes",
                        click:function () {
                            ConfirmDialog.saveProgram();
                            closeConfirmDialog();
                        }
                    },
                    { text:"Discard changes",
                        click:function () {
                            closeConfirmDialog();
                            fun();
                        }
                    },
                    { text:"Cancel",
                        click:function () {
                            closeConfirmDialog();
                        }
                    }
                ]
            });

            $("#confirmDialog").dialog("open");
            if (!ConfirmDialog.isLoggedIn()) {
                $(":button:contains('Save changes')").attr("disabled", "disabled").addClass("ui-state-disabled");
            }
        } else {
            fun();
        }
    }

    function closeConfirmDialog() {
        $("#confirmDialog").dialog("close");
    }

    return ConfirmDialog;
})();