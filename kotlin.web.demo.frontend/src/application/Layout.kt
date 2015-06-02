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

package application

import jquery.jq
import org.w3c.dom.HTMLElement
import utils.*
import views.find
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.dom.hasClass


object Layout {
    val resizableProjectTreeHolder = document.getElementById("examples-list-resizer") as HTMLElement;
    val projectTreeDisplayButton = document.getElementById("accordion-display-button") as HTMLElement;
    val gridElement = document.getElementById("g-grid") as HTMLElement;
    val gridTopElement = document.getElementById("grid-top") as HTMLElement;
    val fullscreenButton = document.getElementById("fullscreen-button") as HTMLElement;
    val toolbox = document.getElementById("toolbox") as HTMLElement;
    val argumentsButton = document.getElementById("argumentsButton") as HTMLElement;

    init {
        projectTreeDisplayButton.onclick = {
            projectTreeDisplayButton.toggleClass("accordion-hidden");
            toolbox.toggleClass("accordion-hidden");
            if (resizableProjectTreeHolder.isVisible()) {
                jq(resizableProjectTreeHolder).hide();
            } else {
                jq(resizableProjectTreeHolder).show();
            }
            onAccordionResized();
            updateGridConfigurationInLocalStorage()
        };

        toolbox.style.minWidth = ({
            var childWidth = 0;
            jq(toolbox).children().toArray().forEach {
                childWidth += jq(it).outerWidth();
            };
            (childWidth + 10).toString() + "px";
        })();

        fullscreenButton.onclick = {
            if (isFullscreenMode()) {
                jq("[fullscreen-sensible]").removeClass("fullscreen");
                jq(fullscreenButton).find(".text").html("Fullscreen");

                jq(gridElement).css("height", "");
                jq(gridTopElement).css("height", "");

                jq("#grid-bottom").css("height", "");
                jq("#result-tabs").css("height", "");
                jq(".tab-space").css("height", "");

                jq("#examples-list-resizer").css("width", "");
                if (resizableProjectTreeHolder.isVisible()) {
                    jq("#workspace").css("margin-left", "");
                } else {
                    jq("#workspace").css("margin-left", 0);
                }
                updateEditorHeightAndRefreshEditor();
            } else {
                jq("[fullscreen-sensible]").addClass("fullscreen");
                jq(fullscreenButton).find(".text").html("Exit fullscreen");
                updateGridHeightFullscreen();
            }
            updateProjectTreeMaxWidth();
            updateGridConfigurationInLocalStorage();
        };

        window.onresize = {
            updateProjectTreeMaxWidth();
            if (isFullscreenMode()) {
                updateGridHeightFullscreen();
            }
        };

        jq(resizableProjectTreeHolder).resizable(json(
                "handles" to "e",
                "minWidth" to 215,
                "stop" to { updateGridConfigurationInLocalStorage() },
                "resize" to { onAccordionResized() }
        ))

        jq("#grid-bottom").resizable(json(
                "handles" to "n",
                "minHeight" to jq("#statusBarWrapper").outerHeight() + jq(".result-tabs-footer").outerHeight(),
                "start" to {
                    jq("#grid-bottom").resizable("option", "minHeight",
                            jq("#statusBarWrapper").outerHeight() + jq(".result-tabs-footer").outerHeight())

                    jq("#grid-bottom").resizable("option", "maxHeight",
                            jq("#g-grid").height().toInt() - jq(Elements.argumentsInputElement).outerHeight() - jq("#toolbox").outerHeight())
                },
                "stop" to {updateGridConfigurationInLocalStorage()},
                "resize" to {onOutputViewResized()}
        )) ;

        argumentsButton.onclick = {
            if (jq(argumentsButton).hasClass("active")) {
                jq(argumentsButton).removeClass("active");
                jq(Elements.argumentsInputElement).hide();
            } else {
                jq(argumentsButton).addClass("active");
                jq(Elements.argumentsInputElement).show()
            }
            updateEditorHeightAndRefreshEditor();
            updateGridConfigurationInLocalStorage();
        };

        var gridConfiguration: dynamic = localStorage.getItem("gridConfiguration");
        if (gridConfiguration != null) {
            gridConfiguration = JSON.parse(gridConfiguration);
            if (gridConfiguration.fullscreenMode) fullscreenButton.click();
            if (!gridConfiguration.argumentsVisible) argumentsButton.click();
            if (!gridConfiguration.projectTreeVisible) projectTreeDisplayButton.click();
            jq("#examples-list-resizer").width(gridConfiguration.examplesWidth);
            onAccordionResized();
            jq("#grid-bottom").height(gridConfiguration.gridBottomHeight);
            onOutputViewResized();
        }
        updateEditorHeightAndRefreshEditor() ;

        updateProjectTreeMaxWidth()
    }

    fun init(){

    }


    private fun updateProjectTreeMaxWidth() {
        jq(resizableProjectTreeHolder).resizable("option", "maxWidth", jq("#grid-top").width().toInt() - parseInt(toolbox.style.minWidth));
    }

    private fun onAccordionResized() {
        if (resizableProjectTreeHolder.isVisible()) {
            jq("#workspace").css("margin-left", jq(resizableProjectTreeHolder).outerWidth());
        } else {
            jq("#workspace").css("margin-left", 0);
        }
    }


    private fun onOutputViewResized() {
        jq("#grid-bottom").css("top", "");
        jq("#result-tabs").css("height", jq("#grid-bottom").height().toInt() - jq("#statusBarWrapper").outerHeight());
        jq("#test-wrapper").find(".consoleOutput").css("height", jq("#program-output").height().toInt() - jq("#unit-test-statistic").outerHeight(true));
        jq(".tab-space").css("height", jq("#result-tabs").height().toInt() - jq(".result-tabs-footer").outerHeight());

        var gridHeight = jq("#g-grid").height().toInt();
        var gridTopHeight = gridHeight - jq("#grid-bottom").outerHeight(true) - jq("#grid-nav").outerHeight(true);
        gridTopHeight -= (jq(gridTopElement).outerHeight(true) - jq(gridTopElement).height().toInt());
        jq(gridTopElement).css("height", gridTopHeight);
        updateEditorHeightAndRefreshEditor();
    }


    private fun updateGridConfigurationInLocalStorage() {
        var gridConfiguration = json(
                "examplesWidth" to jq(resizableProjectTreeHolder).width(),
                "gridBottomHeight" to jq("#grid-bottom").height(),
                "fullscreenMode" to isFullscreenMode(),
                "argumentsVisible" to Elements.argumentsInputElement.isVisible(),
                "projectTreeVisible" to resizableProjectTreeHolder.isVisible()
        );
        localStorage.setItem("gridConfiguration", JSON.stringify(gridConfiguration));
    }

    //Calling codemirror refresh from codemirror callback can lead to strange results
    private fun updateEditorHeight() {
        val editorNotification = document.getElementById("editor-notifications") as HTMLElement
        val workspaceHeight = jq(gridTopElement).height().toInt();
        var toolBoxHeight = jq(toolbox).outerHeight();
        var commandLineArgumentsHeight = if (Elements.argumentsInputElement.isVisible()) jq(Elements.argumentsInputElement).outerHeight() else 0;
        var notificationsHeight = if (editorNotification.isVisible()) jq("#editor-notifications").outerHeight() else 0;
        var editorHeight = workspaceHeight - toolBoxHeight - commandLineArgumentsHeight - notificationsHeight;
        (document.getElementById("editordiv") as HTMLElement).style.height = editorHeight.toString() + "px";
    }

    private fun updateEditorHeightAndRefreshEditor() {
        updateEditorHeight();
        Application.editor.refresh()
    }

    private fun updateGridHeightFullscreen() {
        var gridHeight = jq(".global-layout").height().toInt() - jq(".global-toolbox").outerHeight(true);
        gridHeight -= (jq(gridElement).outerHeight(true) - jq(gridElement).height().toInt());
        jq(gridElement).css("height", gridHeight);

        var gridTopHeight = gridHeight - jq("#statusBarWrapper").outerHeight(true) - jq("#result-tabs").outerHeight() - jq("#grid-nav").outerHeight(true);
        gridTopHeight -= (jq(gridTopElement).outerHeight(true) - jq(gridTopElement).height().toInt());
        jq(gridTopElement).css("height", gridTopHeight);
        updateEditorHeightAndRefreshEditor()
    }

    private fun isFullscreenMode(): Boolean {
        return fullscreenButton.hasClass("fullscreen");
    }

}