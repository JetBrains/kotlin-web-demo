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

package org.jetbrains.webdemo;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/9/11
 * Time: 3:32 PM
 */

public class TimeManager {
    private long startTime;
    private long savedTime = 0;

    public TimeManager() {
        startTime = System.nanoTime();
    }

    public void updateStartTime() {
        startTime = System.nanoTime();
    }

    public long getMillisecondsFromStart() {
        return (System.nanoTime() - startTime) / 1000000;
    }

    public void saveCurrentTime() {
        savedTime = System.nanoTime();
    }

    public String getMillisecondsFromSavedTime() {
        return String.valueOf((System.nanoTime() - savedTime) / 1000000);
    }
}
