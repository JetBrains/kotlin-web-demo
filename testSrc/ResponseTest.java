import junit.framework.TestCase;
import org.jetbrains.annotations.Nullable;
import web.view.ukhorskaya.Initializer;
import web.view.ukhorskaya.KotlinHttpServer;

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

    private final String LOCALHOST = "http://localhost/";

    public void testServerStarted() {
        assertTrue("Server didn't start", KotlinHttpServer.isServerRunning());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Initializer.getInstance().initJavaCoreEnvironment();
        if (!KotlinHttpServer.isServerRunning()) {
            KotlinHttpServer.getInstance().startServer();
        }
    }

    public void testIncorrectUrlFormat() throws IOException {
        String expectedResult = "Wrong request: /incorrectUrlFormat";
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

    //Runtime.getRuntime().exec() Exception
    public void test$errors$securityExecutionError() throws IOException, InterruptedException {
        String expectedResult = "Generated classfiles: <br>namespace.class<br><br><font color=\"red\">Exception in thread \"main\" java.security.AccessControlException: access denied (java.io.FilePermission <<ALL FILES>> execute)<br></font><font color=\"red\">\tat java.security.AccessControlContext.checkPermission(AccessControlContext.java:374)<br></font><font color=\"red\">\tat java.security.AccessController.checkPermission(AccessController.java:546)<br></font><font color=\"red\">\tat java.lang.SecurityManager.checkPermission(SecurityManager.java:532)<br></font><font color=\"red\">\tat java.lang.SecurityManager.checkExec(SecurityManager.java:782)<br></font><font color=\"red\">\tat java.lang.ProcessBuilder.start(ProcessBuilder.java:448)<br></font><font color=\"red\">\tat java.lang.Runtime.exec(Runtime.java:593)<br></font><font color=\"red\">\tat java.lang.Runtime.exec(Runtime.java:431)<br></font><font color=\"red\">\tat java.lang.Runtime.exec(Runtime.java:328)<br></font><font color=\"red\">\tat namespace.main(dummy.jet:2)<br></font>";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    //Exception when read file from other directory
    public void test$errors$securityFilePermissionError() throws IOException, InterruptedException {
        String expectedResult = "Generated classfiles: <br>namespace.class<br><br><font color=\"red\">Exception in thread \"main\" java.security.AccessControlException: access denied (java.io.FilePermission test.kt read)<br></font><font color=\"red\">\tat java.security.AccessControlContext.checkPermission(AccessControlContext.java:374)<br></font><font color=\"red\">\tat java.security.AccessController.checkPermission(AccessController.java:546)<br></font><font color=\"red\">\tat java.lang.SecurityManager.checkPermission(SecurityManager.java:532)<br></font><font color=\"red\">\tat java.lang.SecurityManager.checkRead(SecurityManager.java:871)<br></font><font color=\"red\">\tat java.io.File.exists(File.java:731)<br></font><font color=\"red\">\tat namespace.main(dummy.jet:3)<br></font>";
        compareResponseForPostRequest(expectedResult, "run=true", null);
    }

    //Two errors in one line
    public void test$errors$twoErrorsInOneLine() throws IOException, InterruptedException {
        String expectedResult = "[{\"titleName\":\"Expecting ')'\",\"severity\":\"ERROR\",\"className\":\"red_wavy_line\",\"y\":\"{line: 1, ch: 30}\",\"x\":\"{line: 1, ch: 29}\"},{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\",\"severity\":\"ERROR\",\"className\":\"red_wavy_line\",\"y\":\"{line: 1, ch: 15}\",\"x\":\"{line: 1, ch: 14}\"}]";
        compareResponseForPostRequest(expectedResult, "sendData=true", null);
    }

    //Compilation with error
    public void test$errors$compilationWithError() throws IOException, InterruptedException {
        String expectedResult = "<p class=\"newLineClass\"><img src=\"/icons/error.png\"/>ERROR: <font color=\"red\">(1, 44) - Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?</font></p>";
        String data = "fun main(args : Array<String>) { System.err.println(\"ERROR\") }";
        compareResponseForPostRequest(expectedResult, "compile=true", data);
    }

    //run with error
    public void test$errors$runWithError() throws IOException, InterruptedException {
        String expectedResult = "<p class=\"newLineClass\"><img src=\"/icons/error.png\"/>ERROR: <font color=\"red\">(1, 44) - Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?</font></p>";
        String data = "fun main(args : Array<String>) { System.err.println(\"ERROR\") }";
        compareResponseForPostRequest(expectedResult, "run=true", data);
    }

    //Completion after point
    public void test$completion$completionAfterPoint() throws IOException, InterruptedException {
        String expectedResult = "[{\"icon\":\"/icons/method.png\",\"name\":\"setJavaLangAccess()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setSecurityManager(p0 : SecurityManager?...\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"currentTimeMillis()\",\"tail\":\"Long\"},{\"icon\":\"/icons/property.png\",\"name\":\"in\",\"tail\":\"InputStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"runFinalizersOnExit(p0 : Boolean)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"arraycopy(p0 : Any?, p1 : Int, p2 : Any?...\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setProperties(p0 : Properties?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"nullInputStream()\",\"tail\":\"InputStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setIn0(p0 : InputStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setProperty(p0 : String?, p1 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"checkKey(p0 : String?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setErr0(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/property.png\",\"name\":\"err\",\"tail\":\"PrintStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"clearProperty(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/property.png\",\"name\":\"security\",\"tail\":\"SecurityManager?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setErr(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/property.png\",\"name\":\"cons\",\"tail\":\"Console?\"},{\"icon\":\"/icons/method.png\",\"name\":\"getenv()\",\"tail\":\"Map<String?, String?>?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setOut(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getProperty(p0 : String?, p1 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"registerNatives()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getProperties()\",\"tail\":\"Properties?\"},{\"icon\":\"/icons/method.png\",\"name\":\"runFinalization()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setSecurityManager0(p0 : SecurityManager...\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"gc()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getSecurityManager()\",\"tail\":\"SecurityManager?\"},{\"icon\":\"/icons/method.png\",\"name\":\"checkIO()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"console()\",\"tail\":\"Console?\"},{\"icon\":\"/icons/property.png\",\"name\":\"out\",\"tail\":\"PrintStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"getProperty(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"setIn(p0 : InputStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"mapLibraryName(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"identityHashCode(p0 : Any?)\",\"tail\":\"Int\"},{\"icon\":\"/icons/method.png\",\"name\":\"exit(p0 : Int)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"nullPrintStream()\",\"tail\":\"PrintStream?\"},{\"icon\":\"/icons/method.png\",\"name\":\"loadLibrary(p0 : String?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"setOut0(p0 : PrintStream?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getenv(p0 : String?)\",\"tail\":\"String?\"},{\"icon\":\"/icons/method.png\",\"name\":\"currentTimeMillis()\",\"tail\":\"Long\"},{\"icon\":\"/icons/method.png\",\"name\":\"load(p0 : String?)\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"inheritedChannel()\",\"tail\":\"Channel?\"},{\"icon\":\"/icons/method.png\",\"name\":\"initProperties(p0 : Properties?)\",\"tail\":\"Properties?\"},{\"icon\":\"/icons/property.png\",\"name\":\"props\",\"tail\":\"Properties?\"},{\"icon\":\"/icons/method.png\",\"name\":\"initializeSystemClass()\",\"tail\":\"Tuple0\"},{\"icon\":\"/icons/method.png\",\"name\":\"getCallerClass()\",\"tail\":\"Class<out Any?>?\"}]";
        String data = "test=fun main(args : Array<String>) { System. }";
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
        int pos = testName.indexOf("test");
        if (pos != -1) {
            testName = testName.substring(pos + 5);
            testName = testName.replace("$", "/");
        }
        return testName;
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
        String urlPath = LOCALHOST + urlWoLocalhost.replace(" ", "%20");
        if (query != null) {
            urlPath = urlPath + "?" + query;
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
        String filePath = "C://Development/KotlinCompiler/KotlinStandCompiler/testData/" + fileName;
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
        if (resultData.equals("")) {
            System.err.println("Incorrect format for test name: test$path to file with test data (if there is a subdirectory - use $)");
        }
        return resultData.toString();
    }

    private String processString(String inputString) {
        inputString = inputString.replaceAll("    ", " ");
        return inputString.replaceAll("\\r\\n", "");
    }


}
