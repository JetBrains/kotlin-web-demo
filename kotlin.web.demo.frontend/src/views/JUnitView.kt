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

import application.Application
import application.consoleView
import jquery.JQuery
import jquery.jq
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.HTMLUListElement
import utils.*
import views.dialogs.DifferenceDialog
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.text.Regex


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


class JUnitView(
        private val element: HTMLElement,
        private val tabs: JQuery
) {
    var statistic: HTMLElement? = null;
    var statisticText: dynamic = null;
    var navTree: dynamic = null;

    fun setOutput(data: dynamic) {
        element.innerHTML = "";

        tabs.tabs("option", "active", 1);
        navTree = document.createElement("div");
        navTree.id = "test-tree-div";
        element.appendChild(navTree);
        statistic = document.createElement("div") as HTMLElement;
        statistic!!.id = "unit-test-statistic";

        statisticText = document.createElement("span");
        statistic!!.appendChild(statisticText);

        var consoleElement = document.createElement("div") as HTMLDivElement;
        consoleElement.className = "consoleOutput";
        val outputView = OutputView(consoleElement);

        var wrapper = document.createElement("div");
        wrapper.id = "test-wrapper";
        element.appendChild(wrapper);
        wrapper.appendChild(statistic!!);
        wrapper.appendChild(consoleElement);

        if (data.type == "out") {
            if (data.testResults.length != 0) {
                createStatistics(data.testResults);
                createTestTree(data.testResults, outputView);
            } else {
                consoleView.writeException("No test method found");
            }
        } else {
            throw Exception("Unknown data type");
        }
        jq(consoleElement).height(jq(wrapper).height().toInt() - jq(statistic!!).outerHeight(true).toInt());
    }

    fun clear() {
        element.innerHTML = "";
    }

    private fun findCommonPackage(classNames: List<String>): List<String> {
        val sortedClassNames = classNames.sort();
        val firstPackage = sortedClassNames.first().split('.');
        val lastPackage = sortedClassNames.last().split('.');
        var j = 0;
        while (firstPackage.size() > j && lastPackage.size() > j && firstPackage[j] == lastPackage[j]) {
            ++j;
        }
        return firstPackage.slice(0..j - 1);
    }

    fun createTestTree(data: Array<dynamic>, outputView: OutputView) {
        var testsData = hashMapOf<String, dynamic>();
        for (testResult in data) {
            testsData[testResult.className + '.' + testResult.methodName] = json(
                    "output" to testResult.output,
                    "exception" to testResult.exception
            );
        }

        var classNames = arrayListOf<String>();
        for (testResult in data) {
            classNames.add(testResult.className);
        }
        var commonPackage = findCommonPackage(classNames);

        var commonPackageFullName = "";
        var rootNode: dynamic =
                if (!commonPackage.isEmpty()) {
                    commonPackageFullName = commonPackage.join(".")
                    json(
                            "children" to arrayOf<dynamic>(),
                            "name" to commonPackage.last(),
                            "icon" to "ok",
                            "id" to commonPackageFullName
                    );
                } else {
                    commonPackageFullName = "<default package>"
                    json(
                            "children" to arrayOf<dynamic>(),
                            "name" to commonPackageFullName,
                            "icon" to "ok",
                            "id" to commonPackageFullName
                    );
                }

        if (commonPackageFullName == classNames[0]) {
            for (testResult in data) {
                var testNode = json(
                        "children" to arrayOf<dynamic>(),
                        "name" to  getClassNameWithoutAPackage(testResult.methodName),
                        "icon" to  testResult.status.toLowerCase(),
                        "sourceFileName" to  testResult.sourceFileName,
                        "methodPosition" to  testResult.methodPosition,
                        "id" to  testResult.className + '.' + testResult.methodName
                );
                rootNode.children.push(testNode);
                if (testResult.status == Status.ERROR) {
                    rootNode.icon = "error";
                } else if (testResult.status == Status.FAIL && rootNode.icon != "error") {
                    rootNode.icon = "fail";
                }
            }
        } else {
            var classNodes = hashMapOf<String, dynamic>();
            for (testResult in data) {
                if (classNodes[testResult.className] == null) {
                    classNodes[testResult.className] = json(
                            "children" to  arrayOf<dynamic>(),
                            "name" to  getClassNameWithoutAPackage(testResult.className),
                            "icon" to  testResult.status.toLowerCase(),
                            "id" to  testResult.className
                    );
                    rootNode.children.push(classNodes[testResult.className]);
                }
                val classNode = classNodes[testResult.className];
                var testNode = json(
                        "children" to arrayOf<dynamic>(),
                        "name" to testResult.methodName,
                        "icon" to testResult.status.toLowerCase(),
                        "sourceFileName" to testResult.sourceFileName,
                        "methodPosition" to testResult.methodPosition,
                        "id" to testResult.className + '.' + testResult.methodName
                );
                classNode.children.push(testNode);

                if (testResult.status == Status.ERROR) {
                    classNode.icon = "error";
                    rootNode.icon = "error";
                } else if (testResult.status == Status.FAIL && rootNode.icon != "error") {
                    classNode.icon = "fail";
                    rootNode.icon = "fail";
                }
            }
        }

        var tree = document.createElement("ul") as HTMLUListElement;
        tree.id = "test-tree";
        displayTreeNode(rootNode, tree);

        fun printTestOutput(element: HTMLElement, outputView: OutputView) {
            if (jq(element).hasClass("at-no-children")) {
                var testData = testsData[element.id];
                outputView.printMarkedText(testsData[element.id].output);
                if (testData.exception != null) {
                    try {
                        var parsedMessage = parseAssertionErrorMessage(unEscapeString(testData.exception.message));
                        outputView.printErrorLine(testData.exception.fullName + ":" + parsedMessage.assertionMessage);
                        outputView.printErrorLine("");
                        outputView.print("Expected: ");
                        outputView.printErrorLine(parsedMessage.expected);
                        outputView.print("Actual: ");
                        outputView.printErrorLine(parsedMessage.actual);
                        outputView.printError("    ");
                        outputView.element.appendChild(makeDifferenceReference(parsedMessage.expected, parsedMessage.actual));
                        outputView.println("");
                        outputView.println("");
                        outputView.printExceptionBody(testData.exception);
                    } catch (exception: Throwable) {
                        outputView.printException(testData.exception);
                    }
                }
            } else {
                jq(element).children("ul").children("li").toArray().forEach { elem ->
                    printTestOutput(elem, outputView)
                }
            }
        }

        jq(tree).a11yTree(json(
                "toggleSelector" to ".tree-node-header .toggle-arrow",
                "treeItemLabelSelector" to ".tree-node-header .text",
                "onFocus" to { element: Array<dynamic> ->
                    outputView.element.innerHTML = "";
                    printTestOutput(element[0], outputView);
                }
        ));
        jq(tree).find("li[aria-expanded]").attr("aria-expanded", "true");
        navTree.appendChild(tree);
    }

    fun displayTreeNode(node: dynamic, parentElement: HTMLElement) {
        var nodeElement = document.createElement("li");
        nodeElement.className = "tree-node";
        nodeElement.id = node.id;
        parentElement.appendChild(nodeElement);

        var nodeElementHeader = document.createElement("div") as HTMLDivElement;
        nodeElementHeader.className = "tree-node-header";
        nodeElement.appendChild(nodeElementHeader);

        var img = document.createElement("div");
        img.className = "icon " + node.icon;
        nodeElementHeader.appendChild(img);

        var text = document.createElement("div");
        text.className = "text";
        text.textContent = node.name;
        nodeElementHeader.appendChild(text);

        if (node.children.length > 0) {
            var toggle = document.createElement("div");
            toggle.className = "toggle-arrow";
            nodeElementHeader.insertBefore(toggle, nodeElementHeader.firstChild);

            var childrenNode = document.createElement("ul") as HTMLUListElement;
            for (child in node.children) {
                displayTreeNode(child, childrenNode);
            }
            nodeElement.appendChild(childrenNode);
        } else {
            nodeElementHeader.ondblclick = {
                Application.accordion.selectedProjectView!!.getFileViewByName(node.sourceFileName)!!.fireSelectEvent();
                Application.editor.setCursor(node.methodPosition - 1, 0);
                Application.editor.focus();
            }
        }
    }

    fun createStatistics(data: dynamic) {
        var totalTime = 0.0;
        var noOfFailedTest = 0;
        var status = Status.OK;
        for (testInfo in data) {
            totalTime += (testInfo.executionTime as Double) / 1000.0;
            if (testInfo.status == Status.FAIL || testInfo.status == Status.ERROR) {
                ++noOfFailedTest;
                status = Status.ERROR;
            }
        }
        displayStatistic(data.length, noOfFailedTest, totalTime, status);
    }

    fun displayStatistic(testsNumber: Int, testsFailed: Int, time: dynamic, status: Status) {
        if (testsFailed == 0) {
            statisticText.innerHTML = "All tests passed in " + time.toFixed(3) + "s";
        } else {
            statisticText.innerHTML = "Failed: " + testsFailed + " of " + testsNumber + " (in " + time.toFixed(3) + "s)";
        }

        var testsStatusElement = document.createElement("div");
        testsStatusElement.id = "tests-status-bar";
        statistic!!.appendChild(testsStatusElement);

        var backgroundElement = document.createElement("div");
        backgroundElement.className = "background";
        testsStatusElement.appendChild(backgroundElement);

        if (status == Status.ERROR || status == Status.FAIL) {
            backgroundElement.addClass("fail");
        } else if (status == Status.OK) {
            backgroundElement.addClass("ok");
        } else {
            throw Exception("Bad status");
        }
    }

    fun getClassNameWithoutAPackage(fullClassName: String): String {
        var path = fullClassName.split('.');
        return path.last();
    }

    fun parseAssertionErrorMessage(message: String): dynamic {
        val message = message.replace("\n", "</br>");
        var possibleRegExps = arrayOf(
                Regex("(.*)\\.\\s*Expected\\s*<(.*)>\\s*actual\\s*<(.*)>"),
                Regex("(. *)expected:\\s*<(.*)>\\s*but was:\\s*<(.*)>")
        )
        for (regExp in  possibleRegExps) {
            var regExpExecResult = regExp.match(message);
            if (regExpExecResult != null) {
                return json(
                    "assertionMessage" to regExpExecResult.groups[1]!!.value,
                    "expected" to regExpExecResult.groups[2]!!.value,
                    "actual" to regExpExecResult.groups[3]!!.value
                )
            }
        }
        return null;
    }

    fun makeDifferenceReference(expected: String, actual: String): HTMLLinkElement {
        var a = document.createElement("a") as HTMLLinkElement;
        a.textContent = "<click to see a difference>";
        a.onclick = { event ->
            DifferenceDialog.open(expected, actual);
            event.stopPropagation();
        };
        return a;
    }
}

enum class Status(value: String) {
    OK("OK"),
    FAIL("FAIL"),
    ERROR("ERROR")
}