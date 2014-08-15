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

    function JUnitView(element, tabs) {

        var instance = {
            setOutput: function (data) {
                element.empty();
                tabs.tabs("option", "active", 1);
                navTree = document.createElement("div");
                navTree.id = "test-tree-div";
                element.append(navTree);

                console = document.createElement("div");
                console.id = "test-console";
                element.append(console);
                parseTestTree(data);
            }
        };
        return instance;
    }

    function showTestTree(classes) {

        var tree = document.createElement("div");
        tree.id = "test-tree";
        navTree.appendChild(tree);

        for (var i = 0; i < classes.length; i++) {
            var li = document.createElement("h3");
            var img = document.createElement("div");
            if (classes[i].status == "fail") {
                img.className = "fail-img";
            } else if (classes[i].status == "ok") {
                img.className = "ok-img";
            } else if (classes[i].status == "error") {
                img.className = "error-img";
            }

            li.appendChild(img);
            var className = document.createElement("div");
            className.innerHTML = classes[i].name;
            className.className = "header-text";
            li.appendChild(className);
            li.onclick = (function () {
                var lines = [];
                for(var j = 0; j < classes[i].tests.length; j++){
                    for(var k = 0; k < classes[i].tests[j].lines.length; k++) {
                        lines.push(classes[i].tests[j].lines[k]);
                        var p = document.createElement("p");
                        p.innerHTML = classes[i].tests[j].lines[k];
                        console.appendChild(p);
                    }
                }

                function show() {
                    console.innerHTML = "";
                    for (var i = 0; i < lines.length; i++) {
                        var p = document.createElement("p");
                        p.innerHTML = lines[i];
                        console.appendChild(p);
                    }
                }

                return show;
            })();

            tree.appendChild(li);

            var content = document.createElement("div");
            tree.appendChild(content);

            for (var j = 0; j < classes[i].tests.length; j++) {
                var test = document.createElement("div");
                test.className = "test-case";

                img = document.createElement("div");
                if (classes[i].tests[j].status == "fail") {
                    img.className = "fail-img";
                } else if (classes[i].tests[j].status == "ok") {
                    img.className = "ok-img";
                } else if (classes[i].tests[j].status == "error") {
                    img.className = "error-img";
                }

                var testName = document.createElement("span");
                testName.className = "test-name";
                testName.innerHTML = classes[i].tests[j].name;
                testName.onclick = (function () {
                    var lines = classes[i].tests[j].lines;

                    function show() {
                        console.innerHTML = "";
                        for (var i = 0; i < lines.length; i++) {
                            var p = document.createElement("p");
                            p.innerHTML = lines[i];
                            console.appendChild(p);
                        }
                    }

                    return show;
                })();

                test.appendChild(img);
                test.appendChild(testName);
                content.appendChild(test);
            }
        }


        $("#test-tree").accordion({ heightStyle: "content",
            navigation: true
        });
    }

    function getOrCreateClass(classes, classFullName) {
        for (var i = 0; i < classes.length; i++) {
            if (classes[i].fullName == classFullName) {
                return classes[i];
            }
        }

        var className = classFullName.substr(classFullName.lastIndexOf(".") + 1);
        var cl = {fullName: classFullName,
            name: className,
            tests: [],
            status: "ok"
        };
        classes.push(cl);
        return cl;
    }

    function getOrCreateTest(tests, testName) {
        for (var i = 0; i < tests.length; i++) {
            if (tests[i].name == testName) {
                return tests[i];
            }
        }
        var test ={
            name: testName,
            lines: [],
            status: "ok"
        };
        tests.push(test);
        return test;
    }

    function parseTestTree(data) {
        var testClasses = [];
        for (var i = 0; i < data.length; i++) {
            if (data[i].type != "info" && data[i].type != "toggle-info") {

                var text = data[i].text.replace(/&amp;lt;/g, "").replace(/&amp;gt;/g, "").split("<br/>");
                var testStart = /@(.*) started@/;
                var j = 0;

                var testClass = null;
                while (j < text.length) {
                    if (text[j].match(testStart) != null) {
                        var testHeader = text[j].match(testStart)[1];
                        var testNameFormat = /(.*)\((.*)\)/;

                        var fullClassName = testHeader.match(testNameFormat)[2];
                        testClass = getOrCreateClass(testClasses, fullClassName);
                        var test = getOrCreateTest(testClass.tests, testHeader.match(testNameFormat)[1]);
                        j++;

                        while (text[j] != "@" + testHeader + " finished@") {
                            if (text[j] == "@" + testHeader + " failed@") {
                                if (testClass.status != "error") {
                                    testClass.status = "fail";
                                }
                                test.status = "fail";
                            } else if (text[j] == "@" + testHeader + " error@") {
                                testClass.status = "error";
                                test.status = "error";
                            } else {
                                test.lines.push(text[j]);
                            }
                            j++;
                        }
                        j++;


                    } else {
                        var p = document.createElement("p");
                        p.innerHTML = text[j];
                        console.appendChild(p);
                        j++;
                    }
                }
            } else {
                generatedCodeView.setOutput(data[i]);
            }
        }
        showTestTree(testClasses);
    }

    return JUnitView;
})
();