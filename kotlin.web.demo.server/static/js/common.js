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

var KOTLIN_VERSION = "0.12.66";

String.prototype.endsWith = function (a) {
    return -1 !== this.indexOf(a, this.length - a.length);
};

String.prototype.startsWith = function (a) {
    return 0 === this.indexOf(a);
};

String.prototype.capitalize = function () {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

function checkDataForNull(data) {
    return !(data == null || data == undefined);

}

function checkDataForException(data) {
    //consoleView.writeException(data);
    return !(data[0] != null && data[0] != undefined && data[0].exception != undefined);

}

