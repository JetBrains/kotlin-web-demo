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
 * Date: 2/2/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */

function EventsHandler() {
    this.event_listeners = {};
}

EventsHandler.prototype = {

    constructor:EventsHandler,

    addListener:function (type, listener) {
        if (typeof this.event_listeners[type] == "undefined") {
            this.event_listeners[type] = [];
        }

        this.event_listeners[type].push(listener);
    },

    fire:function (type, status, param) {
        if (this.event_listeners[type] instanceof Array) {
            var listeners = this.event_listeners[type];
            for (var i = 0, len = listeners.length; i < len; i++) {
                if (param != null && typeof param != "undefined") {
                    if (status) {
                        if (param[0] != null && typeof param[0] != "undefined"
                            && typeof param[0].exception != "undefined") {
                            this.event_listeners["write_exception"][0](param);
                            listeners[i](false, param);
                        } else {
                            listeners[i](status, param);
                        }
                    } else {
                        listeners[i](status, param);
                    }
                } else {
                    listeners[i](status);
//                listeners[i](false, param);
//                this.event_listeners["write_exception"][0]("Received data is null.");
                }
            }
        }
    }
};