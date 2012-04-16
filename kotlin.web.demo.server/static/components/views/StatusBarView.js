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


var StatusBarView = (function () {

    StatusBarView.Messages = {
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
        run_java_fail:"Can't get program output.",
        run_js_ok:"Translation competed successfully.",
        run_js_fail:"Can't get translation result from server.",
        login_ok:"Log in successful.",
        login_fail:"Log in fail.",
        logout_ok:"Log out successful.",
        logout_fail:"Log out fail.",
        load_program_ok:"Program is loaded.",
        load_program_fail:"Can't load the program from server.",
        load_programs_fail:"Can't load programs from server.",
        load_examples_fail:"Can't load examples from server.",
        generate_link_ok:"Public link is generated.",
        generate_link_fail:"Can't generate the public link for program.",
        delete_program_ok:"Program is deleted.",
        delete_program_fail:"Can't delete the program from server.",
        convert_java_to_kotlin_ok:"Translation result was loaded in editor.",
        convert_java_to_kotlin_fail:"Can't convert you Java file to Kotlin.",
        save_program_ok:"Program was successfully saved.",
        save_program_fail:"Can't save the program on server.",
        loading_highlighting:"Loading highlighting...",
        loading_completion:"Loading completion..",


        get_result_from_applet_fail:"Your browser can't run Java Applets."
    };


    function StatusBarView(element) {

        var instance = {
            setMessage:function (message) {
                element.html(message);
            },
            setError:function (message) {
                element.html("<font color=\"red\">" + message + "</font>");
            }
        };

        return instance;
    }

    return StatusBarView;
})();