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

import jquery.JQuery
import jquery.jq
import utils.jquery.on
import utils.jquery.ui.selectmenu
import utils.jquery.value
import kotlin.browser.document

enum class ConfigurationTypeRunner {
    JAVA,
    JS
}

enum class ConfigurationTypeDependencies {
    STANDARD,
    CANVAS,
    JUNIT
}

enum class ConfigurationMode {
    SERVER,
    ONRUN
}

enum class ConfigurationType(val runner: ConfigurationTypeRunner, val dependencies: ConfigurationTypeDependencies) {
    JAVA(ConfigurationTypeRunner.JAVA, ConfigurationTypeDependencies.STANDARD),
    JS(ConfigurationTypeRunner.JS, ConfigurationTypeDependencies.STANDARD),
    CANVAS(ConfigurationTypeRunner.JS, ConfigurationTypeDependencies.CANVAS),
    JUNIT(ConfigurationTypeRunner.JAVA, ConfigurationTypeDependencies.JUNIT)
}

data class Configuration(val mode: ConfigurationMode, val type: ConfigurationType)

class ConfigurationManager(private val onChange: (Configuration) -> Unit) {
    var configuration = Configuration(ConfigurationMode.ONRUN, ConfigurationType.JAVA)

    fun getConfiguration(): Configuration {
        return configuration
    }

    fun getType(): String {
        return configuration.type.name()
    }

    fun updateConfiguration(type: String) {
        configuration = Configuration(configuration.mode, getTypeFromString(type))
        jq("#runMode").value(type)
        jq("#runMode").selectmenu("refresh")
        if (jq("#runMode").value() == "java" || jq("#runMode").value() == "junit") {
            document.getElementById("generated-code-link")!!.innerHTML = "Generated classfiles"
        } else {
            document.getElementById("generated-code-link")!!.innerHTML = "Generated JavaScript code"
        }
        fireChangeEvent()
    }

    fun getTypeFromString(type: String) = when (type) {
        "js" -> ConfigurationType.JS
        "java" -> ConfigurationType.JAVA
        "junit" -> ConfigurationType.JUNIT
        "canvas" -> ConfigurationType.CANVAS
        else -> throw IllegalArgumentException("Unknown configuration type")
    }

    fun fireChangeEvent() {
        onChange(configuration)
    }

    init {
        jq("#runMode").on("selectmenuchange", {
            var confType = jq("#runMode").value()
            if (confType == "java" || jq("#runMode").value() == "junit") {
                document.getElementById("generated-code-link")!!.innerHTML = "Generated classfiles"
            } else {
                document.getElementById("generated-code-link")!!.innerHTML = "Generated JavaScript code"
            }
            configuration = Configuration(configuration.mode, getTypeFromString(confType))
            fireChangeEvent()
        })

    }
}
