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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.server.KotlinHttpServer;
import org.jetbrains.webdemo.server.ServerSettings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 8/4/11
 * Time: 4:21 PM
 */
public class ResponseTest extends TestCase {

    private final String HOST = "http://" + ServerSettings.HOST + "/";
    private final String TEST_SRC = "C://Development/git-contrib/jet-contrib/WebView/TestModule/testData/";

    public ResponseTest(String name) {
        super(name);
    }

    public void testServerStarted() {
        assertTrue("Server didn't start", KotlinHttpServer.isServerRunning());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerInitializer.getInstance().initJavaCoreEnvironment();
        if (!KotlinHttpServer.isServerRunning()) {
            KotlinHttpServer.startServer();
        }
        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
    }

    public void testIncorrectUrlFormat() throws IOException {
        String expectedResult = "Resource not found. /incorrectUrlFormat";
        String actualResult = getActualResultForRequest("incorrectUrlFormat");
        assertEquals("Wrong result", expectedResult, actualResult);
    }

    //One error
    public void test$errors$oneError() throws IOException, InterruptedException {
        String expectedResult = "[{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\",\"severity\":\"ERROR\",\"className\":\"red_wavy_line\",\"y\":\"{line: 1, ch: 15}\",\"x\":\"{line: 1, ch: 14}\"}]";
        compareResponseForPostRequest(expectedResult, "sendData=true", null);
    }

    //One warning
    public void test$warnings$oneWarning() throws IOException, InterruptedException {
        String expectedResult = "[{\"titleName\":\"Unnecessary safe call on a non-null receiver of type Int\",\"severity\":\"WARNING\",\"className\":\"WARNING\",\"y\":\"{line: 2, ch: 7}\",\"x\":\"{line: 2, ch: 5}\"}]";
        compareResponseForPostRequest(expectedResult, "sendData=true", null);
    }

    public void test$execution$FooOutErr() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"Hello<br/>\",\"type\":\"out\"},{\"text\":\"ERROR<br/>\",\"type\":\"err\"}]";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    public void test$execution$FooOut() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"Hello<br/>\",\"type\":\"out\"}]";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    public void test$execution$FooErr() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"\",\"type\":\"out\"},{\"text\":\"ERROR<br/>\",\"type\":\"err\"}]";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    //Runtime.getRuntime().exec() Exception
    public void test$errors$securityExecutionError() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"\",\"type\":\"out\"},{\"text\":\"Exception in thread \\\"main\\\" java.security.AccessControlException: access denied (java.io.FilePermission &lt;&lt;ALL FILES&gt;&gt; execute)<br/>\\tat java.security.AccessControlContext.checkPermission(AccessControlContext.java:374)<br/>\\tat java.security.AccessController.checkPermission(AccessController.java:546)<br/>\\tat java.lang.SecurityManager.checkPermission(SecurityManager.java:532)<br/>\\tat java.lang.SecurityManager.checkExec(SecurityManager.java:782)<br/>\\tat java.lang.ProcessBuilder.start(ProcessBuilder.java:448)<br/>\\tat java.lang.Runtime.exec(Runtime.java:593)<br/>\\tat java.lang.Runtime.exec(Runtime.java:431)<br/>\\tat java.lang.Runtime.exec(Runtime.java:328)<br/>\\tat namespace.main(dummy.jet:2)<br/>\",\"type\":\"err\"}]";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }


    //Exception when read file from other directory
    public void test$errors$securityFilePermissionError() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"\",\"type\":\"out\"},{\"text\":\"Exception in thread \\\"main\\\" java.security.AccessControlException: access denied (java.io.FilePermission test.kt read)<br/>\\tat java.security.AccessControlContext.checkPermission(AccessControlContext.java:374)<br/>\\tat java.security.AccessController.checkPermission(AccessController.java:546)<br/>\\tat java.lang.SecurityManager.checkPermission(SecurityManager.java:532)<br/>\\tat java.lang.SecurityManager.checkRead(SecurityManager.java:871)<br/>\\tat java.io.File.exists(File.java:731)<br/>\\tat namespace.main(dummy.jet:3)<br/>\",\"type\":\"err\"}]";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    //Two errors in one line
    public void test$errors$twoErrorsInOneLine() throws IOException, InterruptedException {
        String expectedResult = "[{\"titleName\":\"Expecting ')'\",\"severity\":\"ERROR\",\"className\":\"red_wavy_line\",\"y\":\"{line: 1, ch: 30}\",\"x\":\"{line: 1, ch: 29}\"},{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\",\"severity\":\"ERROR\",\"className\":\"red_wavy_line\",\"y\":\"{line: 1, ch: 15}\",\"x\":\"{line: 1, ch: 14}\"}]";
        compareResponseForPostRequest(expectedResult, "sendData=true", null);
    }

    //Compilation with error
    public void test$errors$compilationWithError() throws IOException, InterruptedException {
        String expectedResult = "[{\"message\":\"(1, 44) - Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\",\"type\":\"ERROR\"}]";
        String data = "fun main(args : Array<String>) { System.err.println(\"ERROR\") }";
        compareResponseForPostRequest(expectedResult, "compile=true", data);
    }

    //run with error
    public void test$errors$runWithError() throws IOException, InterruptedException {
        String expectedResult = "[{\"message\":\"(1, 44) - Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\",\"type\":\"ERROR\"}]";
        String data = "fun main(args : Array<String>) { System.err.println(\"ERROR\") }";
        compareResponseForPostRequest(expectedResult, "run=true", data);
    }

    public void test$timeout$RunTimeout() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>demo/namespace.class<br/>demo/Main.class<br/>\",\"type\":\"info\"},{\"text\":\"Program was terminated after 5s.\",\"type\":\"err\"}]";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    //Completion after point
    public void test$completion$completionAfterPoint() throws IOException, InterruptedException {
        String expectedResult = "[{\"icon\":\"/static/icons/method.png\",\"name\":\"setJavaLangAccess()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setSecurityManager(p0 : SecurityManager?...\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"nanoTime()\",\"tail\":\"Long\"},{\"icon\":\"/icons/property.png\",\"name\":\"in\",\"tail\":\"InputStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"runFinalizersOnExit(p0 : Boolean)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"arraycopy(p0 : Any?, p1 : Int, p2 : Any?...\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setProperties(p0 : Properties?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"nullInputStream()\",\"tail\":\"InputStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setIn0(p0 : InputStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setProperty(p0 : String?, p1 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"checkKey(p0 : String?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setErr0(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/property.png\",\"name\":\"err\",\"tail\":\"PrintStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"clearProperty(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/property.png\",\"name\":\"security\",\"tail\":\"SecurityManager?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setErr(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/property.png\",\"name\":\"cons\",\"tail\":\"Console?\"},{\"icon\":\"/icons/method.png\",\"name\":\"getenv()\",\"tail\":\"Map<String?, String?>?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setOut(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getProperty(p0 : String?, p1 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"registerNatives()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getProperties()\",\"tail\":\"Properties?\"},{\"icon\":\"/icons/method.png\",\"name\":\"runFinalization()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setSecurityManager0(p0 : SecurityManager...\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"gc()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getSecurityManager()\",\"tail\":\"SecurityManager?\"},{\"icon\":\"/icons/method.png\",\"name\":\"checkIO()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"console()\",\"tail\":\"Console?\"},{\"icon\":\"/icons/property.png\",\"name\":\"out\",\"tail\":\"PrintStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"getProperty(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setIn(p0 : InputStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"mapLibraryName(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"identityHashCode(p0 : Any?)\",\"tail\":\"Int\"},{\"icon\":\"/icons/method.png\",\"name\":\"exit(p0 : Int)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"nullPrintStream()\",\"tail\":\"PrintStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"loadLibrary(p0 : String?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setOut0(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getenv(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"nanoTime()\",\"tail\":\"Long\"},{\"icon\":\"/icons/method.png\",\"name\":\"load(p0 : String?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"inheritedChannel()\",\"tail\":\"Channel?\"},{\"icon\":\"/icons/method.png\",\"name\":\"initProperties(p0 : Properties?)\",\"tail\":\"Properties?\"},{\"icon\":\"/icons/property.png\",\"name\":\"props\",\"tail\":\"Properties?\"},{\"icon\":\"/icons/method.png\",\"name\":\"initializeSystemClass()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getCallerClass()\",\"tail\":\"Class<out Any?>?\"}]";
        String data = "text=fun main(args : Array<String>) { System. }";
        String actualResult = getActualResultForRequest(getUrlFromFileName(getNameByTestName()), data, "complete=true&cursorAt=0,40");
        assertNotNull(actualResult);
        //compareResponseForPostRequest(expectedResult, "complete=true&cursorAt=0,40", data);
    }

    private void compareResponseForPostRequest(String expectedResult, String query, @Nullable String data) throws IOException {
        String fileName = getNameByTestName() + ".kt";
        if (data == null) {
            data = getDataFromFile(fileName);
        }

        String actualResult = getActualResultForRequest(getUrlFromFileName("testUrl"), "text=" + data, query);
        assertEquals("Wrong result", expectedResult, actualResult);
    }

    //Get name of file to load content for test
    private String getNameByTestName() {
        String testName = this.getName();
        testName = ResponseUtils.substringAfter(testName, "test$");
        testName = testName.replace("$", "/");
        return testName;
    }

    public void test$errors$verifyError() throws IOException, InterruptedException {
        String fileName = getNameByTestName();
        String data = getDataFromFile(fileName + ".kt");

        String actualResult = getActualResultForRequest("", "text=" + data, "sendData=true");
        String expectedResult = getExpectedResult(fileName);
        assertEquals("Wrong result", expectedResult, actualResult);
    }

    private String getExpectedResult(String fileName) throws IOException {
        return getDataFromFile(fileName + ".txt");
    }

    /*public void testErrorInfile() throws IOException, InterruptedException {
        String fileName = "Foo.kt";
        String data = getDataFromFile(fileName);

        String actualResult = getActualResultForRequest(getUrlFromFileName(fileName), "text=" + data, "sendData=true");
        String expectedResult = getExpectedResult(fileName);
        assertEquals("Wrong result", expectedResult, actualResult);
    }

    private String getExpectedResult(String fileName) throws IOException {
        return getDataFromFile(fileName + ".txt");
    }*/

    private String getActualResultForRequest(String urlWoLocalhost) throws IOException {
        return getActualResultForRequest(urlWoLocalhost, null, null);
    }

    //data - data to send in post request, query - query for url
    private String getActualResultForRequest(String urlWoLocalhost, @Nullable String data, @Nullable String query) throws IOException {
        String urlPath = HOST + urlWoLocalhost.replace(" ", "%20");
        if (query != null) {
            urlPath = urlPath + "?sessionId=555&" + query;
        }
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        if (data != null) {
            urlConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
        }

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        } catch (FileNotFoundException e) {
            in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
        }

        String str;
        StringBuilder result = new StringBuilder();
        while ((str = in.readLine()) != null) {
            result.append(str);
        }
        in.close();

        return result.toString();
    }

    private String getUrlFromFileName(String fileName) {
        return "path=" + fileName;
    }

    private String getDataFromFile(String fileName) throws IOException {
        String filePath = TEST_SRC + fileName;
        File file = new File(filePath);
        StringBuilder resultData = new StringBuilder();
        if (file.exists()) {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String tmp = "";
            while ((tmp = reader.readLine()) != null) {
                resultData.append(tmp);
                if (!fileName.endsWith(".txt")) {
                    resultData.append("<br>");
                }
            }
        }
        String result = resultData.toString();
        if (result.equals("")) {
            System.err.println("Incorrect format for test name: test$path to file with test data (if there is a subdirectory - use $)");
        }
        return result;
    }

    private String processString(String inputString) {
        inputString = inputString.replaceAll("    ", " ");
        return inputString.replaceAll("\\r\\n", "");
    }


}
