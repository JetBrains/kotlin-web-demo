// We specify the package of our tests

import com.google.common.io.Files;
import junit.framework.TestCase;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

// This is the driver's import. You'll use this for instantiating a
// browser and making it do what you need.

// Selenium-IDE add the Pattern module because it's sometimes used for
// regex validations. You can remove the module if it's not used in your
// script.

public class SeleniumTest extends TestCase {

    private final String TEST_SRC = "C://Development/contrib/jet-contrib/WebView/TestModule/testData/";

    // We create our Selenium test case
    private WebDriver driver;
    private WebElement statusBar;
    private WebElement console;
    private WebElement run;
    private WebElement refresh;
    private WebElement runJs;
    private WebElement accordion;
    private Wait<WebDriver> wait;
    private WebElement editor;

    private boolean isRunTested = true;

    public void setUp() throws Exception {
        super.setUp();
        /*ChromeDriverService service = new ChromeDriverService.Builder()
                .usingChromeDriverExecutable(new File("C:\\Users\\Natalia.Ukhorskaya\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe"))
                .usingAnyFreePort()
                .build();
        service.start();*/
        /*DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability("chrome.binary", "C:\\Users\\Natalia.Ukhorskaya\\AppData\\Local\\Google\\Chrome\\Application\\");
        driver = new ChromeDriver(capabilities);
        driver = new RemoteWebDriver(service.getUrl(),
                DesiredCapabilities.chrome());*/
        driver = new FirefoxDriver();
//        driver.get("http://kotlin-demo.jetbrains.com");
        driver.get("http://localhost");
        statusBar = driver.findElement(By.id("statusbar"));
        console = driver.findElement(By.id("console"));
        run = driver.findElement(By.id("run"));
        refresh = driver.findElement(By.id("refreshGutters"));
        runJs = driver.findElement(By.id("runJS"));
        accordion = driver.findElement(By.id("accordion"));

        WebElement arguments = driver.findElement(By.id("arguments"));
        editor = driver.findElement(By.id("code"));
        WebElement problems = driver.findElement(By.id("code"));

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 500, 500);
    }

    public void testAllSimplesExamples() throws IOException, InterruptedException {
        isRunTested = true;

        driver.findElement(By.id("Hello,_world!")).click();
        final WebElement el = driver.findElement(By.id("Simplest_version&head=Hello,_world!"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Simplest version", "Hello,_world!", "Hello, world!");
        testExampleRun("Reading a name from the command line", "Hello,_world!", "Hello, guest!");
        testExampleRun("Reading many names from the command line", "Hello,_world!", "Hello, guest1!\n" +
                "Hello, guest2!\n" +
                "Hello, guest3!");
        testExampleRun("A multi-language Hello", "Hello,_world!", "Salut!");
        testExampleRun("An object-oriented Hello", "Hello,_world!", "Hello, guest1");

        driver.findElement(By.id("Basic_syntax_walk-through")).click();
        final WebElement el2 = driver.findElement(By.id("Use_a_conditional_expression&head=Basic_syntax_walk-through"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Use a conditional expression", "Basic_syntax_walk-through", "20");
        testExampleRun("Null-checks", "Basic_syntax_walk-through", "6");
        testExampleRun("is-checks and automatic casts", "Basic_syntax_walk-through", "3\n" +
                "null");
        testExampleRun("Use a while-loop", "Basic_syntax_walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "guest4");
        testExampleRun("Use a for-loop", "Basic_syntax_walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "\n" +
                "guest1\n" +
                "guest2\n" +
                "guest3");
        testExampleRun("Use ranges and in", "Basic_syntax_walk-through", "OK\n" +
                "1 2 3 4 5 \n" +
                "Out: array has only 3 elements. x = 4\n" +
                "Yes: array contains aaa\n" +
                "No: array doesn't contains ddd");
        testExampleRun("Use when", "Basic_syntax_walk-through", "Greeting\n" +
                "One\n" +
                "Long\n" +
                "Not a string\n" +
                "Unknown");
    }

    public void testAllDifficultExamples() throws IOException, InterruptedException {
        isRunTested = true;
        driver.findElement(By.id("Longer_examples")).click();
        final WebElement el3 = driver.findElement(By.id("Life&head=Longer_examples"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el3.isDisplayed();
            }
        });

        Thread.sleep(500);

        testDifficultExampleRun("99 Bottles of Beer");
        testDifficultExampleRun("HTML Builder");
        testDifficultExampleRun("Life", true);
        testDifficultExampleRun("Maze", true);
    }

    public void testAllExamplesRunJs() throws IOException, InterruptedException {
        isRunTested = false;

        driver.findElement(By.id("Hello,_world!")).click();
        final WebElement el = driver.findElement(By.id("Simplest_version&head=Hello,_world!"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Simplest version", "Hello,_world!", "Hello, world!");
        testExampleRun("Reading a name from the command line", "Hello,_world!", "Hello, guest!");
        testExampleRun("Reading many names from the command line", "Hello,_world!", "Hello, guest1!\n" +
                "Hello, guest2!\n" +
                "Hello, guest3!");
//        testExampleRun("A multi-language Hello", "Hello,_world!", "Salut!");
        testExampleRun("A multi-language Hello", "Hello,_world!", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Unsupported when condition class org.jetbrains.jet.lang.psi.JetWhenConditionWithExpression");
        testExampleRun("An object-oriented Hello", "Hello,_world!", "Hello, guest1");

        driver.findElement(By.id("Basic_syntax_walk-through")).click();
        final WebElement el2 = driver.findElement(By.id("Use_a_conditional_expression&head=Basic_syntax_walk-through"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Use a conditional expression", "Basic_syntax_walk-through", "20");
        testExampleRun("Null-checks", "Basic_syntax_walk-through", "6");
        testExampleRun("is-checks and automatic casts", "Basic_syntax_walk-through", "3\n" +
                "null");
        testExampleRun("Use a while-loop", "Basic_syntax_walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "guest4");
        testExampleRun("Use a for-loop", "Basic_syntax_walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "\n" +
                "guest1\n" +
                "guest2\n" +
                "guest3");
        testExampleRun("Use ranges and in", "Basic_syntax_walk-through", "OK\n" +
                "1 2 3 4 5 \n" +
                "Out: array has only 3 elements. x = 4\n" +
                "Yes: array contains aaa\n" +
                "No: array doesn't contains ddd");
        testExampleRun("Use when", "Basic_syntax_walk-through", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Undefined descriptor: .java.lang.System.currentTimeMillis");

        driver.findElement(By.id("Longer_examples")).click();
        final WebElement el3 = driver.findElement(By.id("Life&head=Longer_examples"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el3.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("99 Bottles of Beer", "Longer_examples", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "@NotNull method org/jetbrains/k2js/translate/reference/PropertyAccessTranslator.getGetterDescriptor must not return null");
        testExampleRun("HTML Builder", "Longer_examples", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Argument 0 for @NotNull parameter of org/jetbrains/k2js/translate/general/Translation.translateExpression must not be null");
        testExampleRun("Life", "Longer_examples", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "com.google.dart.compiler.backend.js.ast.JsIf cannot be cast to com.google.dart.compiler.backend.js.ast.JsExprStmt");
        testExampleRun("Maze", "Longer_examples", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Undefined descriptor: .java.util.HashMap.");
    }

    public void testAllSimplestExamplesRunWhenServerOn() throws IOException, InterruptedException {
        WebElement el = driver.findElement(By.className("applet-disable"));
        el.click();

        Thread.sleep(500);

        testAllSimplesExamples();
        testAllExamplesRunJs();
    }

    public void testAllDifficultExamplesRunWhenServerOn() throws IOException, InterruptedException {
        WebElement el = driver.findElement(By.className("applet-disable"));
        el.click();

        Thread.sleep(500);

        testAllDifficultExamples();
    }

    public void testAllSimplestExamplesRunWhenAppletOn() throws IOException, InterruptedException {
        WebElement el = driver.findElement(By.className("applet-enable"));
        el.click();

        Thread.sleep(500);

        testAllSimplesExamples();
        testAllExamplesRunJs();
    }

    public void testAllDifficultExamplesRunWhenAppletOn() throws IOException, InterruptedException {
        WebElement el = driver.findElement(By.className("applet-enable"));
        el.click();

        Thread.sleep(500);

        testAllDifficultExamples();
    }

    public void testErrorsAndWarningsWithResfreshButton() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //One error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);

        //Two error in one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 2);

        //One warning
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out?.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkWarningElemements(1, 1);

        //One warning and one error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);
        checkWarningElemements(1, 1);

        //One warning and one error at one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"); val a = 10\\n" +
                " val b = 10\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);
        checkWarningElemements(1, 2);
    }

    public void testErrorsAndWarningsWithRunButton() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //One error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);

        //Two error in one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 2);

        //One warning
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out?.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        run.click();
        Thread.sleep(500);
        testOutputString("Hello, world!");
        checkWarningElemements(1, 1);

        //One warning and one error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);
        checkWarningElemements(1, 1);

        //One warning and one error at one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"); val a = 10\\n" +
                " val b = 10\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);
        checkWarningElemements(1, 2);
    }

    public void testErrorsAndWarningsWithRunJsButton() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //One error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        runJs.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);

        //Two error in one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        runJs.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 2);

        //One warning
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out?.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        runJs.click();
        Thread.sleep(500);
        checkWarningElemements(1, 1);
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Compilation competed successfully.") || statusBar.getText().equals("Your program has terminated with an exception."));
            }
        });
        testOutputString("Hello, world!");

        //One warning and one error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        runJs.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);
        checkWarningElemements(1, 1);

        //One warning and one error at one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"); val a = 10\\n" +
                " val b = 10\\n" +
                "}" +
                "\");");

        runJs.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);
        checkWarningElemements(1, 2);
    }

    public void testErrorsAndWarningsWhenServerOn() throws IOException, InterruptedException {
        WebElement el = driver.findElement(By.className("applet-disable"));
        el.click();

        Thread.sleep(500);

        testErrorsAndWarningsWithResfreshButton();
        testErrorsAndWarningsWithRunButton();
        testErrorsAndWarningsWithRunJsButton();
    }

    public void testErrorsAndWarningsWhenAppletOn() throws IOException, InterruptedException {
        WebElement el = driver.findElement(By.className("applet-enable"));
        el.click();

        Thread.sleep(500);

        testErrorsAndWarningsWithResfreshButton();
        testErrorsAndWarningsWithRunButton();
        testErrorsAndWarningsWithRunJsButton();
    }

    public int getNumberOfErrorGutters() {
        WebElement problems = driver.findElement(By.id("gutter"));
        List<WebElement> errors = problems.findElements(By.className("ERRORgutter"));
        return errors.size();
    }

    public int getNumberOfErrorsInProblemView() {
        WebElement problems = driver.findElement(By.id("problems"));
        List<WebElement> errors = problems.findElements(By.className("problemsViewError"));
        return errors.size();
    }

    public void checkErrorsElemements(int errorGutter, int problemView) {
        assertEquals(problemView, getNumberOfErrorsInProblemView());
        assertEquals(errorGutter, getNumberOfErrorGutters());
    }

    public int getNumberOfWarningGutters() {
        WebElement problems = driver.findElement(By.id("gutter"));
        List<WebElement> errors = problems.findElements(By.className("WARNINGgutter"));
        return errors.size();
    }

    public int getNumberOfWarningInProblemView() {
        WebElement problems = driver.findElement(By.id("problems"));
        List<WebElement> errors = problems.findElements(By.className("problemsViewWarning"));
        return errors.size();
    }

    public void checkWarningElemements(int errorGutter, int problemView) {
        assertEquals(problemView, getNumberOfWarningInProblemView());
        assertEquals(errorGutter, getNumberOfWarningGutters());
    }


    private String readResultFromFile(String filePath) throws IOException {
        return Files.toString(new File(TEST_SRC + File.separator + filePath), Charset.forName("UTF-8"));
    }


    private void testDifficultExampleRun(String name) throws IOException, InterruptedException {
        testDifficultExampleRun(name, false);
    }

    private void testDifficultExampleRun(String name, boolean isSpaceReplaced) throws IOException, InterruptedException {
        testExampleRun(name, "Longer_examples", readResultFromFile("examples" + File.separator + name + ".txt"), isSpaceReplaced);
    }

    private String getExampleNameByTestName() {
        String testName = this.getName();
        testName = ResponseUtils.substringAfter(testName, "testExample$");
        testName = testName.replace("$", " ");
        testName = testName.replace("MINUS", "-");
        return testName;
    }


    private void testExampleRun(final String exampleName, final String headName, final String result) throws InterruptedException {
        testExampleRun(exampleName, headName, result, false);
    }

    private void testExampleRun(final String exampleName, final String headName, final String result, boolean isSpaceReplaced) throws InterruptedException {
        final WebElement example = accordion.findElement(By.id(exampleName.replaceAll("([ ])", "_") + "&head=" + headName));
        example.click();

        Thread.sleep(500);
        /*wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Example is loaded.") || statusBar.getText().equals("Can't get example from server."));
            }
        });*/

        if (isRunTested) {
            run.click();
        } else {
            runJs.click();
        }

        Thread.sleep(500);

        /*wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Running...") || statusBar.getText().equals("Your program has terminated with an exception."));
            }
        });*/

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Compilation competed successfully.") || statusBar.getText().equals("Your program has terminated with an exception."));
            }
        });

        if (getName().contains("DifficultExamples")) {
            if (isSpaceReplaced) {
                testOneFileExampleWithReplaceSpacesAndLineSeparator(result);
            } else {
                testOneFileExampleWithReplaceLineSeparator(result);
            }
        } else {
            if (statusBar.getText().equals("Your program has terminated with an exception.")) {
                testErrorString(result);
            } else {
                testOutputString(result);
            }
        }
    }

    private void testOutputString(String expectedResult) {
        String actualResult = "";
        List<WebElement> list = console.findElements(By.tagName("p"));
        for (WebElement webElement : list) {
            if (webElement.getAttribute("class").equals("")) {
                actualResult = webElement.getText();
                break;
            }
        }
        assertEquals(expectedResult, actualResult);
    }

    private void testErrorString(String expectedResult) {
        String actualResult = "";
        List<WebElement> list = console.findElements(By.tagName("p"));
        for (WebElement webElement : list) {
            if (webElement.getAttribute("class").equals("consoleViewError")) {
                actualResult = webElement.getText();
                break;
            }
        }
        assertEquals(expectedResult, actualResult);
    }

    private void testOneFileExampleWithReplaceSpacesAndLineSeparator(String expectedResult) {
        String actualResult = "";
        List<WebElement> list = console.findElements(By.tagName("p"));
        for (WebElement webElement : list) {
            if (webElement.getAttribute("class").equals("")) {
                actualResult = webElement.getText();
                break;
            }
        }
        assertEquals(expectedResult, replaceSpacesAndLineSeparators(actualResult));
    }

    private void testOneFileExampleWithReplaceLineSeparator(String expectedResult) {
        String actualResult = "";
        List<WebElement> list = console.findElements(By.tagName("p"));
        for (WebElement webElement : list) {
            if (webElement.getAttribute("class").equals("")) {
                actualResult = webElement.getText();
                break;
            }
        }
        assertEquals(expectedResult, replaceLineSeparators(actualResult));
    }

    private String replaceSpacesAndLineSeparators(String str) {
        return str.replaceAll("([ ])", "_").replaceAll("([\n])", System.getProperty("line.separator"));
    }

    private String replaceLineSeparators(String str) {
        return str.replaceAll("([\n])", System.getProperty("line.separator"));
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        driver.quit();
    }


}
