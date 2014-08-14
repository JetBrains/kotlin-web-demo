/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
 * Created by Semyon.Atamas on 8/13/2014.
 */


var JUnitView = (function () {
    var console;
    var navTree;

    function JUnitView(_console, _navTree) {
        console = _console;
        navTree = _navTree;


        var instance = {
            setOutput: function (data) {
                setOutput(data);
            }
        };
        return instance;
    }

    function showTree() {

    }

    function showTestTree(classes) {
        var tree = document.createElement("div");
        tree.id = "test-tree";
        navTree.append(tree);
        for (var i = 0; i < classes.length; i++) {
            var li = document.createElement("h3");
            var className = document.createElement("div");
            className.innerHTML = classes[i].name;
            li.appendChild(className);
            tree.appendChild(li);
            for(var j = 0; j < classes[i].tests.length; j++){
                var test = document.createElement("div");
                var testName = document.createElement("span");
                testName.innerHTML = classes[i].tests[j].name;
                test.appendChild(testName);
                tree.appendChild(test);
            }

        }
        $("#test-tree").accordion({ heightStyle: "content",
            navigation: true,
            icons: {
                activeHeader: "examples-open-folder-icon",
                header: "examples-closed-folder-icon"
            }
        });
    }

    function parseTestTree(data) {
        var testClasses = [];
        for (var i = 0; i < data.length; i++) {
            if (data[i].type == "out") {
                var text = data[i].text.split("<br/>");
                var testStart = /@(.*) started@/;
                var j = 0;

                var testClass = null;
                while (j < text.length) {
                    if (text[j].match(testStart) != null) {
                        var testHeader = text[j].match(testStart)[1];
                        var testNameFormat = /(.*)\((.*)\)/;

                        var className = testHeader.match(testNameFormat)[2];
                        if (testClass != null) {
                            if (className != testClass.name) {
                                testClasses.push(testClass);
                                testClass = {name: className,
                                    tests: [],
                                    failed: false
                                }
                            }
                        } else {
                            testClass = {name: className,
                                tests: [],
                                failed: false
                            }
                        }

                        var test = {name: testHeader.match(testNameFormat)[1],
                            lines: [],
                            failed: false};


                        j++;

                        while (text[j] != "@" + testHeader + " finished@") {
                            if (text[j] == "@" + testHeader + " failed@") {
                                testClass.failed = true;
                                test.failed = true;
                            } else {
                                test.lines.push(text[j]);
                            }
                            j++;
                        }
                        j++;

                        testClass.tests.push(test);


                    } else {
                        var p = document.createElement("p");
                        p.innerHTML = text[i];
                        console.append(p);
                        j++;
                    }
                }

                if (testClass != null) {
                    testClasses.push(testClass);
                }
            }
        }
        showTestTree(testClasses);
    }

    function setOutput(data) {
        parseTestTree(data)
    }

    return JUnitView;
})
();