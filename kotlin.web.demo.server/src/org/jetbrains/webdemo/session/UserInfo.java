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

package org.jetbrains.webdemo.session;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String name = "";
    private String id = "";
    private String type = "";
    private boolean isLogged = false;

    public void login(String name, String id, String type) {
        isLogged = true;
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public boolean isLogin() {
        return isLogged;
    }

    public void logout() {
        name = "";
        id = "";
        isLogged = false;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

}
