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

package org.jetbrains.web.demo.test;

import com.sun.javaws.security.JavaWebStartSecurity;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.web.demo.test.completion.CompletionTest;
import org.jetbrains.web.demo.test.examples.HighlightExamplesTest;
import org.jetbrains.web.demo.test.examples.RunExamplesTest;
import org.jetbrains.web.demo.test.highlighting.HighlightingTest;
import org.jetbrains.web.demo.test.run.RunTest;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.session.SessionInfo;
//import org.jetbrains.webdemo.translator.WebDemoConfigApplet;
import org.jetbrains.webdemo.translator.WebDemoConfigServer;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;
import sun.applet.AppletSecurity;

import java.io.IOException;
import java.util.Random;

/**
 * @author Natalia.Ukhorskaya
 */

public class TestApplet extends TestCase {

    /* Get highlighting under security manager
     * TODO Check that problem is only in guice library
     */
    public void test() throws IOException, InterruptedException {
       /* InitializerApplet.getInstance().initJavaCoreEnvironment();
        ErrorWriter.ERROR_WRITER = ErrorWriterInApplet.getInstance();
        Initializer.INITIALIZER = InitializerApplet.getInstance();
        WebDemoTranslatorFacade.LOAD_JS_LIBRARY_CONFIG = new WebDemoConfigApplet(Initializer.INITIALIZER.getEnvironment().getProject());

        MainApplet.SESSION_INFO = new SessionInfo("applet" + new Random().nextInt());
        System.setSecurityManager(new AppletSecurity());
        assertEquals("[]", new MainApplet().getHighlighting("fun main(args : Array<String>) { }"));*/
    }

}
