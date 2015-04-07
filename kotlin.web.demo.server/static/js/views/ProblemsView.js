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


var ProblemsView = function (element, /*Nullable*/ tabs) {

    var instance = {
        addMessages: function () {
            addMessagesToProblemsView();
        },
        onProjectChange: function (newProject) {
        },
        clear: function () {
            element.innerHTML = "";
        },
        setCursor: function(filename, line, character){

        }
    };

    function addMessagesToProblemsView() {
        var fileNodes = $("#problems-tree").find(">li");
        var expandedStatus = {};
        for(var i = 0; i < fileNodes.length; ++i){
            expandedStatus[fileNodes[i].id] = fileNodes[i].getAttribute("aria-expanded");
        }

        element.innerHTML = "";
        var treeElement = document.createElement("ul");
        treeElement.id = "problems-tree";
        element.appendChild(treeElement);

        var projectData = accordion.getSelectedProject();
        for (var i = 0; i < projectData.getFiles().length; i++) {
            var file = projectData.getFiles()[i];
            if (file.errors.length > 0) {
                var fileId = file.name.replace(/ /g, "%20") + "_problems";
                var fileProblemsNode = {
                    children: [],
                    name: file.name,
                    icon: "kotlin-file-icon",
                    id: fileId
                };

                for (var j = 0; j < file.errors.length; j++) {
                    var error = file.errors[j];
                    var errorNode = {
                        children: [],
                        name: error.severity.toLowerCase().capitalize() + ":(" + (error.interval.start.line + 1) + ", " + error.interval.start.ch + ") " + unEscapeString(error.message),
                        icon: error.severity,
                        id: fileId + "_" + error.severity + "_" + error.interval.start.line + "_" + error.interval.start.ch,
                        filename: file.name,
                        line: error.interval.start.line,
                        ch: error.interval.start.ch
                    };
                    fileProblemsNode.children.push(errorNode);
                }
                displayTreeNode(fileProblemsNode, treeElement);
            }
        }

        $(treeElement).a11yTree({
            toggleSelector: ".tree-node-header .toggle-arrow",
            treeItemLabelSelector: '.tree-node-header .text',
            onFocus: function(item){
                if($(document.activeElement).hasClass("tree-node")) {
                    item.focus();
                }
            }
        });

        $("#problems-tree").find("li[aria-expanded]").attr("aria-expanded", "true");
        for(var id in expandedStatus){
            if(expandedStatus[id] == "false"){
                document.getElementById(id).setAttribute("aria-expanded", "false");
            }
        }
    }

    function displayTreeNode(node, parentElement) {
        var nodeElement = document.createElement("li");
        nodeElement.className = "tree-node";
        nodeElement.id = node.id;
        nodeElement.setAttribute("tabindex", "-1");
        if(node.expanded == "true"){
            nodeElement.setAttribute("aria-expanded", "true");
        }
        parentElement.appendChild(nodeElement);

        var nodeElementHeader = document.createElement("div");
        nodeElementHeader.className = "tree-node-header";
        nodeElement.appendChild(nodeElementHeader);

        var img = document.createElement("div");
        img.className = "icon " + node.icon.toLowerCase();
        nodeElementHeader.appendChild(img);

        var text = document.createElement("div");
        text.className = "text";
        text.textContent = node.name;
        nodeElementHeader.appendChild(text);

        if (node.children.length > 0) {
            nodeElementHeader.className += " file-problems-header";
            var toggle = document.createElement("div");
            toggle.className = "toggle-arrow";
            nodeElementHeader.insertBefore(toggle, nodeElementHeader.firstChild);

            var childrenNode = document.createElement("ul");
            for (var i = 0; i < node.children.length; ++i) {
                displayTreeNode(node.children[i], childrenNode);
            }
            nodeElement.appendChild(childrenNode);
        } else{
            nodeElement.ondblclick = function(){
                instance.setCursor(node.filename, node.line, node.ch);
            };
            nodeElement.onkeyup =  function(event){
                if (event.keyCode == 13) {
                    event.stopPropagation();
                    instance.setCursor(node.filename, node.line, node.ch);
                }
            };
        }
    }


    return instance;
};