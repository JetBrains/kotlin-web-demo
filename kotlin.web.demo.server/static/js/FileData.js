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
 * Created by Semyon.Atamas on 10/13/2014.
 */

var FileData = (function () {

    function FileData(/*nullable*/ data) {
        var instance = {
            name: "",
            content: "",
            modifiable: true,
            errors: [],
            type: "Kotlin",
            publicId: "",
            modified: "false",
            setContent: function (content) {
                instance.modified = true;
                instance.content = content;
                instance.onContentChange()
            },
            onContentChange: function () {
            }
        };

        if (data != null) {
            instance.name = data.name;
            instance.content = data.content;
            instance.modifiable = data.modifiable;
            instance.publicId = data.publicId;
        }
        return instance;
    }

    return FileData;
})();