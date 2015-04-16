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
import Project

/**
 * Created by Semyon.Atamas on 4/16/2015.
 */

data class ValidationResult(val valid: Boolean, val message: String = "")

native
class InputDialogView(title: String, inputText: String, buttonText: String) {
    var validate: (String) -> ValidationResult
    var open: (callback: (String) -> Unit, defaultValue: String) -> Unit
}

native
trait ProjectView{
    fun getProjectData(): Project
}

native
val loginView: dynamic = noImpl