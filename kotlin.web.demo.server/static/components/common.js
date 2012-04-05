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
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */

var BEFORE_EXIT = "The changes you made to the program will be lost when you change an example. Do you want to leave the page?";

var KOTLIN_VERSION = "0.0.0";
var WEB_DEMO_VERSION = "0.0.0";

function forEachInArrayWithArgs(arr, data, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(data, arr[i]);
        i++;
    }
}

function forEachInArray(arr, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(arr[i]);
        i++;
    }
}

function unEscapeString(str) {
    str = replaceAll(str, "&amp;", "&");
    return str;
}

var tagsToReplace = {
    '&':'&amp;',
    '<':'&lt;',
    '>':'&gt;'
};

function replaceTag(tag) {
    return tagsToReplace[tag] || tag;
}

function safe_tags_replace(str) {
    return str.replace(/[&<>]/g, replaceTag);
}

function replaceAll(str, replaced, replacement) {
    try {
        return str.replace(new RegExp(replaced, 'g'), replacement)
    } catch (e) {
        //todo alert(str);
        return str;
    }
}