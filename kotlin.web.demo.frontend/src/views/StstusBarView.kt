/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package views

import org.w3c.dom.HTMLElement


class StatusBarView(private val element: HTMLElement){
    fun setStatus(statusMessage: ActionStatusMessage, vararg parameters: String){
        var message = statusMessage.message
        parameters.forEach { message = message.replaceFirst("@parameter@", it) }
        element.innerHTML = message;
    }
}

enum class ActionStatusMessage(val message: String){
    get_header_fail("Can't get project header"),
    delete_file_fail("Can't delete file"),
    rename_file_fail("Can't rename file"),
    rename_project_fail("Can't rename project"),
    add_project_fail("Can't add new project"),
    load_headers_ok("Projects headers were loaded"),
    load_headers_fail("Can't get projects headers"),
    load_header_ok("Project header was loaded"),
    load_header_fail("Can't get project header"),

    load_project_ok("Project content loaded"),
    load_project_fail("Can't load project content from server"),
    load_help_for_words_ok("Help for words was loaded from server"),
    load_help_for_words_fail("Can't get help for words from server"),
    change_configuration_ok("Configuration was changed."),
    change_configuration_fail("Can't change configuration"),
    get_highlighting_ok("@parameter@ errors/warnings"),
    get_highlighting_fail("Can't get errors/warnings"),
    get_completion_ok("Completion proposal list was loaded from server"),
    get_completion_fail("Can't get completion proposal list from server"),
    run_java_ok("Compilation completed successfully"),
    run_java_fail("Can't get program output"),
    run_js_ok("Translation completed successfully"),
    run_js_fail("Can't get translation result from server"),
    login_ok("Log in successful"),
    login_fail("Log in fail"),
    logout_ok("Log out successful"),
    logout_fail("Log out fail"),
    load_programs_fail("Can't load programs from server"),
    load_examples_fail("Can't load examples from server"),
    generate_link_ok("Public link is generated"),
    generate_link_fail("Can't generate the public link for program"),
    delete_program_ok("Program is deleted"),
    delete_program_fail("Can't delete project"),
    convert_java_to_kotlin_ok("Translation result was loaded in editor"),
    convert_java_to_kotlin_fail("Can't convert you Java file to Kotlin"),
    save_program_ok("Program was successfully saved"),
    save_program_fail("Can't save the program on server"),
    loading_highlighting("Loading highlighting..."),
    loading_completion("Loading completion..")
}