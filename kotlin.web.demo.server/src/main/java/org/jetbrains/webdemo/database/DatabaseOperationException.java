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

package org.jetbrains.webdemo.database;


/**
 * Created by Semyon.Atamas on 9/15/2014.
 */
public class DatabaseOperationException extends Exception {
    public DatabaseOperationException(){
        super();
    }
    public DatabaseOperationException(String message){
        super(message);
    }
    public DatabaseOperationException(Throwable exception){
        super(exception);
    }
    public DatabaseOperationException(String message, Throwable cause){
        super(message, cause);
    }
}
