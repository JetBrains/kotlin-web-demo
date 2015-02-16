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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.test.completion.CompletionTest;
import org.jetbrains.webdemo.test.examples.HighlightExamplesTest;
import org.jetbrains.webdemo.test.examples.RunExamplesTest;
import org.jetbrains.webdemo.test.highlighting.HighlightingTest;
import org.jetbrains.webdemo.test.j2kconverter.J2KConverterTest;
import org.jetbrains.webdemo.test.run.RunTest;

public class TestAll extends TestCase {

    public static TestSuite suite() {
        ApplicationSettings.LOAD_TEST_VERSION_OF_EXAMPLES = true;
        TestSuite suite = new TestSuite(
                HighlightingTest.class,
                CompletionTest.class,
                RunTest.class,
                J2KConverterTest.class
        );
        suite.addTest(HighlightExamplesTest.suite());
        suite.addTest(RunExamplesTest.suite());
        return suite;
    }

}
