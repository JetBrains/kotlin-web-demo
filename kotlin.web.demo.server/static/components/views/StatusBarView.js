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


var StatusBarView = (function () {

    var Messages = {
        load_example_ok:"Example is loaded.",
        load_example_fail:"Can't load the example from server.",
        load_help_for_examples_ok:"Help for examples was loaded from server.",
        load_help_for_examples_fail:"Can't get help for examples from server.",
        load_help_for_words_ok:"Help for words was loaded from server.",
        load_help_for_words_fail:"Can't get help for words from server.",
        change_configuration_ok:"Configuration was changed.",
        change_configuration_fail:"Can't change configuration.",
        get_highlighting_ok:"Errors and warnings were loaded.",
        get_highlighting_fail:"Can't get errors/warnings.",
        get_completion_ok:"Completion proposal list was loaded from server.",
        get_completion_fail:"Can't get completion proposal list from server.",
        run_java_ok:"Compilation competed successfully.",
        run_java_fail:"Can't get program output from server.",
        run_js_ok:"Translation competed successfully.",
        login_ok:"Log in successful.",
        login_fail:"Log in fail.",
        logout_ok:"Log out successful.",
        logout_fail:"Log out fail.",
        load_program_ok:"Program is loaded.",
        load_program_fail:"Can't load the program from server.",
        generate_link_ok:"Public link is generated.",
        generate_link_fail:"Can't generate the public link for program.",
        delete_program_ok:"Program is deleted.",
        delete_program_fail:"Can't delete the program from server.",
        convert_java_to_kotlin_ok:"Look kotlin program in editor.",
        convert_java_to_kotlin_fail:"Can't convert you java file to kotlin.",
        save_program_ok:"Program was successfully saved.",
        save_program_fail:"Can't save the program on server.",
        loading_highlighting:"Loading highlighting...",
        loading_completion:"Loading completion..",


        get_result_from_applet_fail:"Your browser can't run Java Applets."
    };

    var eventHandler = new EventsHandler();

    function StatusBarView() {

        var instance = {
            addListener:function (name, f) {
                eventHandler.addListener(name, f);
            },
            fire:function (name, param) {
                eventHandler.fire(name, param);
            },
            setStatusBarMessage:function (message) {
                $("#statusbar").html(message);
            },
            loadExample:function (status) {
                if (status) setMessage(Messages.load_example_ok);
                else setMessage(Messages.load_example_fail);
            },
            loadProgram:function (status) {
                if (status) setMessage(Messages.load_program_ok);
                else setMessage(Messages.load_program_fail);
            },
            loadHelpForExamples:function (status) {
                if (status) setMessage(Messages.load_help_for_examples_ok);
                else setMessage(Messages.load_help_for_examples_fail);
            },
            loadHelpForWords:function (status) {
                if (status) setMessage(Messages.load_help_for_words_ok);
                else setMessage(Messages.load_help_for_words_fail);
            },
            changeConfiguration:function (status) {
                if (status) setMessage(Messages.change_configuration_ok);
                else setMessage(Messages.change_configuration_fail);
            },
            processHighlighting:function (status) {
                if (status) setMessage(Messages.get_highlighting_ok);
                else setMessage(Messages.get_highlighting_fail);
            },
            processCompletion:function (status) {
                if (status) setMessage(Messages.get_completion_ok);
                else setMessage(Messages.get_completion_fail);
            },
            processOutput:function (status) {
                if (status) setMessage(Messages.run_java_ok);
                else setMessage(Messages.run_java_fail);
            },
            processOutputForJs:function (status) {
                if (status) setMessage(Messages.run_js_ok);
                else setMessage(Messages.run_js_fail);
            },
            login:function (status, name) {
                if (status && name == "[\"null\"]") {

                } else if (status && name != "[\"null\"]") {
                    setMessage(Messages.login_ok);
                }
                else {
                    setMessage(Messages.login_fail);
                }
            },
            generatePublicLink:function (status) {
                if (status) setMessage(Messages.generate_link_ok);
                else setMessage(Messages.generate_link_fail);
            },
            logout:function (status) {
                if (status) setMessage(Messages.logout_ok);
                else setMessage(Messages.logout_fail);
            },
            deleteProgram:function (status) {
                if (status) setMessage(Messages.delete_program_ok);
                else setMessage(Messages.delete_program_fail);
            },
            saveProgram:function (status) {
                if (status) setMessage(Messages.save_program_ok);
                else setMessage(Messages.save_program_fail);
            },
            processConverterResult:function (status) {
                if (status) setMessage(Messages.convert_java_to_kotlin_ok);
                else setMessage(Messages.convert_java_to_kotlin_fail);
            },
            processCursorActivity:function (status, data) {
                if (data[0] != "") {
                    setMessage(data[0]);
                }
            },
            loadHighlighting:function (param) {
                if (param[1] != Configuration.mode.ONRUN) {
                    setMessage(Messages.loading_highlighting);
                }
            },
            loadCompletion:function (param) {
                if (param[1] != Configuration.mode.ONRUN) {
                    setMessage(Messages.loading_completion);
                }
            }

        };

        return instance;
    }

    function setMessage(message) {
        $("#statusbar").html(message);
    }

    function setError(message) {
        $("#statusbar").html("<font color=\"red\">" + message + "</font>");
    }

    return StatusBarView;
})();