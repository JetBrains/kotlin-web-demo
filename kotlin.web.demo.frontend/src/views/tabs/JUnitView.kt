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

package views.tabs

import application.Application
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import providers.Status
import providers.TestResult
import utils.a11yTree
import utils.jquery.JQuery
import utils.jquery.jq
import utils.unEscapeString
import views.dialogs.DifferenceDialog
import kotlin.browser.document
import kotlin.collections.set
import kotlin.dom.hasClass
import kotlin.js.json

class JUnitView(
        private val element: HTMLElement,
        private val tabs: JQuery
) {

    fun setOutput(testResults: List<TestResult>) {
        element.innerHTML = ""

        tabs.tabs("option", "active", 1)
        val navTree = document.createElement("div")
        navTree.id = "test-tree-div"
        element.appendChild(navTree)


        var consoleElement = document.createElement("div") as HTMLDivElement
        consoleElement.className = "consoleOutput"
        val outputView = OutputView(consoleElement)

        var wrapper = document.createElement("div")
        wrapper.id = "test-wrapper"
        element.appendChild(wrapper)
        val statistic = createStatistics(testResults)
        wrapper.appendChild(statistic)
        wrapper.appendChild(consoleElement)

        if (testResults.isNotEmpty()) {
            createStatistics(testResults)
            navTree.appendChild(createTestTree(testResults, outputView))
        } else {
            Application.consoleView.writeException("No test method found")
        }
        jq(consoleElement).height(jq(wrapper).height().toInt() - jq(statistic).outerHeight(true).toInt())
    }

    fun clear() {
        element.innerHTML = ""
    }

    private fun findCommonPackage(classNames: List<String>): List<String> {
        val sortedClassNames = classNames.sorted()
        val firstPackage = sortedClassNames.first().split('.')
        val lastPackage = sortedClassNames.last().split('.')
        var j = 0
        while (firstPackage.size > j && lastPackage.size > j && firstPackage[j] == lastPackage[j]) {
            ++j
        }
        return firstPackage.slice(0..j - 1)
    }

    fun createTestTree(data: List<TestResult>, outputView: OutputView): HTMLElement {
        var testsData = hashMapOf<String, TestResult>()
        for (testResult in data) {
            testsData[testResult.className + '.' + testResult.methodName] = testResult
        }

        var classNames = arrayListOf<String>()
        for (testResult in data) {
            classNames.add(testResult.className)
        }
        var commonPackage = findCommonPackage(classNames)

        var commonPackageFullName: String
        var rootNode: dynamic =
                if (!commonPackage.isEmpty()) {
                    commonPackageFullName = commonPackage.joinToString(".")
                    json(
                            "children" to arrayOf<dynamic>(),
                            "name" to commonPackage.last(),
                            "icon" to "ok",
                            "id" to commonPackageFullName
                    )
                } else {
                    commonPackageFullName = "<default package>"
                    json(
                            "children" to arrayOf<dynamic>(),
                            "name" to commonPackageFullName,
                            "icon" to "ok",
                            "id" to commonPackageFullName
                    )
                }

        if (commonPackageFullName == classNames[0]) {
            for (testResult in data) {
                val testNode = json(
                        "children" to arrayOf<dynamic>(),
                        "name" to  getClassNameWithoutAPackage(testResult.methodName),
                        "icon" to  testResult.status.toLowerCase(),
                        "sourceFileName" to  testResult.sourceFileName,
                        "methodPosition" to  testResult.methodPosition,
                        "id" to  testResult.className + '.' + testResult.methodName
                )
                rootNode.children.push(testNode)
                if (testResult.status == Status.ERROR.name) {
                    rootNode.icon = "error"
                } else if (testResult.status == Status.FAIL.name && rootNode.icon != "error") {
                    rootNode.icon = "fail"
                }
            }
        } else {
            var classNodes = hashMapOf<String, dynamic>()
            for (testResult in data) {
                if (classNodes[testResult.className] == null) {
                    classNodes[testResult.className] = json(
                            "children" to  arrayOf<dynamic>(),
                            "name" to  getClassNameWithoutAPackage(testResult.className),
                            "icon" to  testResult.status.toString().toLowerCase(),
                            "id" to  testResult.className
                    )
                    rootNode.children.push(classNodes[testResult.className])
                }
                val classNode = classNodes[testResult.className]
                var testNode = json(
                        "children" to arrayOf<dynamic>(),
                        "name" to testResult.methodName,
                        "icon" to testResult.status.toString().toLowerCase(),
                        "sourceFileName" to testResult.sourceFileName,
                        "methodPosition" to testResult.methodPosition,
                        "id" to testResult.className + '.' + testResult.methodName
                )
                classNode.children.push(testNode)

                if (testResult.status == Status.ERROR.name) {
                    classNode.icon = "error"
                    rootNode.icon = "error"
                } else if (testResult.status == Status.FAIL.name && rootNode.icon != "error") {
                    classNode.icon = "fail"
                    rootNode.icon = "fail"
                }
            }
        }

        var tree = document.createElement("ul") as HTMLUListElement
        tree.id = "test-tree"
        displayTreeNode(rootNode, tree)

        fun printTestOutput(element: HTMLElement) {
            if (element.hasClass("at-no-children")) {
                val testData = testsData[element.id]!!
                outputView.printMarkedText(testData.output)

                val comparisonFailure = testData.comparisonFailure
                if (comparisonFailure != null) {
                    val parsedMessage = ParsedAssertionMessage(
                            parseAssertionErrorMessage(unEscapeString(testData.comparisonFailure!!.message))?.message ?: "",
                            testData.comparisonFailure!!.expected,
                            testData.comparisonFailure!!.actual)
                    outputView.printErrorLine(comparisonFailure.fullName + ": " + parsedMessage.message)
                    outputView.printErrorLine("")
                    outputView.print("Expected: ")
                    outputView.printErrorLine(parsedMessage.expected)
                    outputView.print("Actual: ")
                    outputView.printErrorLine(parsedMessage.actual)
                    outputView.printError("    ")
                    outputView.element.appendChild(createDifferenceReference(parsedMessage.expected, parsedMessage.actual))
                    outputView.println("")
                    outputView.println("")
                    outputView.printExceptionBody(testData.comparisonFailure)
                }

                if(testData.exception != null){
                    outputView.printException(testData.exception)
                }
            } else {
                jq(element).children("ul").children("li").toArray().forEach {
                    printTestOutput(it)
                }
            }
        }

        jq(tree).a11yTree(json(
                "toggleSelector" to ".tree-node-header .toggle-arrow",
                "treeItemLabelSelector" to ".tree-node-header .text",
                "onFocus" to { element: Array<dynamic> ->
                    outputView.element.innerHTML = ""
                    printTestOutput(element[0])
                }
        ))
        jq(tree).find("li[aria-expanded]").attr("aria-expanded", "true")
        return tree
    }

    fun displayTreeNode(node: dynamic, parentElement: HTMLElement) {
        var nodeElement = document.createElement("li")
        nodeElement.className = "tree-node"
        nodeElement.id = node.id
        parentElement.appendChild(nodeElement)

        var nodeElementHeader = document.createElement("div") as HTMLDivElement
        nodeElementHeader.className = "tree-node-header"
        nodeElement.appendChild(nodeElementHeader)

        var img = document.createElement("div")
        img.className = "icon " + node.icon
        nodeElementHeader.appendChild(img)

        var text = document.createElement("div")
        text.className = "text"
        text.textContent = node.name
        nodeElementHeader.appendChild(text)

        if (node.children.length > 0) {
            var toggle = document.createElement("div")
            toggle.className = "toggle-arrow"
            nodeElementHeader.insertBefore(toggle, nodeElementHeader.firstChild)

            var childrenNode = document.createElement("ul") as HTMLUListElement
            for (child in node.children) {
                displayTreeNode(child, childrenNode)
            }
            nodeElement.appendChild(childrenNode)
        } else {
            nodeElementHeader.ondblclick = {
                Application.accordion.selectedProjectView!!.getFileViewByName(node.sourceFileName)!!.fireSelectEvent()
                Application.editor.setCursor(node.methodPosition - 1, 0)
                Application.editor.focus()
            }
        }
    }


    fun getClassNameWithoutAPackage(fullClassName: String): String = fullClassName.split('.').last()

    fun parseAssertionErrorMessage(message: String): ParsedAssertionMessage? {
        var possibleRegExps = arrayOf(
                Regex("(.*)\\.\\s*Expected\\s*<(.*)>\\s*actual\\s*<(.*)>"),
                Regex("(.*)\\s*expected:\\s*<(.*)>\\s*but was:\\s*<(.*)>")
        )
        for (regExp in  possibleRegExps) {
            var regExpExecResult = regExp.find(message.replace("\n", "</br>"))
            if (regExpExecResult != null) {
                return ParsedAssertionMessage(
                        regExpExecResult.groups[1]!!.value,
                        regExpExecResult.groups[2]!!.value,
                        regExpExecResult.groups[3]!!.value
                )
            }
        }
        return null
    }

    fun createStatistics(data: List<TestResult>): HTMLDivElement {
        val totalTime = data.fold (0.0) { time, testResult -> time + testResult.executionTime / 1000.0 }
        val noOfFailedTest = data.count { it.status != Status.OK.name }
        val status = if (noOfFailedTest > 0) Status.FAIL else Status.OK
        val message = if (noOfFailedTest == 0) {
            "All tests passed in ${totalTime.toFixed(3)}s"
        } else {
            "Failed: ${noOfFailedTest} of ${data.size} in ${totalTime.toFixed(3)}s"
        }
        return document.create.div {
            id = "unit-test-statistic"
            span {
                +message
            }
            div {
                id = "tests-status-bar"
                div("background ${status.name.toLowerCase()}")
            }
        }
    }

    fun createDifferenceReference(expected: String, actual: String): HTMLDivElement = document.create.div {
        +"<click to see a difference>"
        classes = setOf("link")
        onClickFunction = { event ->
            DifferenceDialog.open(expected, actual)
            event.stopPropagation()
        }
    }
}

data class ParsedAssertionMessage(
        val message: String,
        val expected: String,
        val actual: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun Double.toFixed(noOfDigits: Int): Double = asDynamic().toFixed(noOfDigits)