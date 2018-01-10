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

package application.layout

import application.Application
import application.elements.Elements
import org.w3c.dom.HTMLElement
import utils.isVisible
import utils.jquery.jq
import utils.toggleClass
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.dom.hasClass
import kotlin.js.json


object Layout {
    private val resizableProjectTreeHolder = document.getElementById("examples-list-resizer") as HTMLElement
    private val projectTreeDisplayButton = document.getElementById("accordion-display-button") as HTMLElement
    private val gridElement = document.getElementById("g-grid") as HTMLElement
    private val gridTopElement = document.getElementById("grid-top") as HTMLElement
    private val fullscreenButton = document.getElementById("fullscreen-button") as HTMLElement
    private val toolbox = document.getElementById("toolbox") as HTMLElement
    private val argumentsButton = document.getElementById("argumentsButton") as HTMLElement

    init {
        projectTreeDisplayButton.onclick = {
            projectTreeDisplayButton.toggleClass("accordion-hidden")
            toolbox.toggleClass("accordion-hidden")
            if (resizableProjectTreeHolder.isVisible()) {
                jq(resizableProjectTreeHolder).hide()
            } else {
                jq(resizableProjectTreeHolder).show()
            }
            Application.editor.refresh()
            updateGridConfigurationInLocalStorage()
        }

        toolbox.style.minWidth = ({
            var childWidth = 0
            jq(toolbox).children().toArray().forEach {
                childWidth += jq(it).outerWidth()
            }
            (childWidth + 10).toString() + "px"
        })()

        fullscreenButton.onclick = {
            if (isFullscreenMode()) {
                jq("[fullscreen-sensible]").removeClass("fullscreen")
                jq(fullscreenButton).find(".text").html("Fullscreen")

                jq(gridElement).css("height", "")
                jq(gridTopElement).css("height", "")

                jq("#grid-bottom").css("height", "")
                jq("#result-tabs").css("height", "")
                jq(".tab-space").css("height", "")

                jq("#examples-list-resizer").css("width", "")
                if (resizableProjectTreeHolder.isVisible()) {
                    jq("#workspace").css("margin-left", "")
                } else {
                    jq("#workspace").css("margin-left", 0)
                }
                Application.editor.refresh()
            } else {
                jq("[fullscreen-sensible]").addClass("fullscreen")
                jq(fullscreenButton).find(".text").html("Exit fullscreen")
                updateGridHeightFullscreen()
            }
            updateProjectTreeMaxWidth()
            updateGridConfigurationInLocalStorage()
        }

        window.onresize = {
            updateProjectTreeMaxWidth()
            if (isFullscreenMode()) {
                updateGridHeightFullscreen()
            }
        }

        jq(resizableProjectTreeHolder).resizable(json(
                "handles" to "e",
                "minWidth" to 215,
                "stop" to {
                    updateGridConfigurationInLocalStorage()
                    Application.editor.refresh()
                }
        ))

        jq("#grid-bottom").resizable(json(
                "handles" to "n",
                "minHeight" to jq("#statusBarWrapper").outerHeight() + jq(".result-tabs-footer").outerHeight(),
                "start" to {
                    jq("#grid-bottom").resizable("option", "minHeight",
                            jq("#statusBarWrapper").outerHeight() + jq(".result-tabs-footer").outerHeight())
                    jq("#grid-bottom").resizable("option", "maxHeight", jq("#workspace-container").height())
                },
                "stop" to {
                    updateGridConfigurationInLocalStorage()
                    Application.editor.refresh()
                }
        ))

        argumentsButton.onclick = {
            if (jq(argumentsButton).hasClass("active")) {
                jq(argumentsButton).removeClass("active")
                jq(Elements.argumentsInputElement).hide()
            } else {
                jq(argumentsButton).addClass("active")
                jq(Elements.argumentsInputElement).show()
            }
            Application.editor.refresh()
            updateGridConfigurationInLocalStorage()
        }

        var gridConfiguration: dynamic = localStorage.getItem("gridConfiguration")
        if (gridConfiguration != null) {
            gridConfiguration = JSON.parse(gridConfiguration)
            if (gridConfiguration.fullscreenMode) fullscreenButton.click()
            if (!gridConfiguration.argumentsVisible) argumentsButton.click()
            if (!gridConfiguration.projectTreeVisible) projectTreeDisplayButton.click()
            jq("#examples-list-resizer").width(gridConfiguration.examplesWidth)
            jq("#grid-bottom").height(gridConfiguration.gridBottomHeight)
        }
        Application.editor.refresh()

        updateProjectTreeMaxWidth()
    }

    fun init(){

    }

    private fun updateProjectTreeMaxWidth() {
        jq(resizableProjectTreeHolder).resizable("option", "maxWidth", jq("#grid-top").width().toInt() - toolbox.style.minWidth.substringBefore("px").toInt())
    }


    private fun updateGridConfigurationInLocalStorage() {
        val gridConfiguration = json(
                "examplesWidth" to jq(resizableProjectTreeHolder).width().toInt(),
                "gridBottomHeight" to jq("#grid-bottom").height().toInt(),
                "fullscreenMode" to isFullscreenMode(),
                "argumentsVisible" to Elements.argumentsInputElement.isVisible(),
                "projectTreeVisible" to resizableProjectTreeHolder.isVisible()
        )
        localStorage.setItem("gridConfiguration", JSON.stringify(gridConfiguration))
    }

    private fun updateGridHeightFullscreen() {
        var gridHeight = jq(".global-layout").height().toInt() - jq(".global-toolbox").outerHeight(true)
        gridHeight -= (jq(gridElement).outerHeight(true) - jq(gridElement).height().toInt())
        jq(gridElement).css("height", gridHeight)

        var gridTopHeight = gridHeight - jq("#statusBarWrapper").outerHeight(true) - jq("#result-tabs").outerHeight() - jq("#grid-nav").outerHeight(true)
        gridTopHeight -= (jq(gridTopElement).outerHeight(true) - jq(gridTopElement).height().toInt())
        jq(gridTopElement).css("height", gridTopHeight)
        Application.editor.refresh()
    }

    private fun isFullscreenMode(): Boolean {
        return fullscreenButton.hasClass("fullscreen")
    }

}