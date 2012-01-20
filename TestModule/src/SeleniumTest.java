// We specify the package of our tests

import com.google.common.io.Files;
import junit.framework.TestCase;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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
    private WebElement runJs;
    private WebElement accordion;
    private Wait<WebDriver> wait;

    private boolean isRunTested = true;

    public void setUp() throws Exception {
        super.setUp();
        driver = new FirefoxDriver();
        driver.get("http://localhost");
        statusBar = driver.findElement(By.id("statusbar"));
        console = driver.findElement(By.id("console"));
        run = driver.findElement(By.id("run"));
        runJs = driver.findElement(By.id("runJS"));
        accordion = driver.findElement(By.id("accordion"));

        WebElement arguments = driver.findElement(By.id("arguments"));
        WebElement editor = driver.findElement(By.id("code"));
        WebElement problems = driver.findElement(By.id("code"));

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 500, 500);
    }

    public void testAllSimplesExamples() throws IOException, InterruptedException {
        isRunTested = true;
        testExampleRun("Simplest version", "Hello, world!");
        testExampleRun("Reading a name from the command line", "Hello, guest!");
        testExampleRun("Reading many names from the command line", "Hello, guest1!\n" +
                "Hello, guest2!\n" +
                "Hello, guest3!");
        testExampleRun("A multi-language Hello", "Salut!");
        testExampleRun("An object-oriented Hello", "Hello, guest1");

        driver.findElement(By.id("Basic syntax walk-through")).click();
        final WebElement el = driver.findElement(By.id("Use a conditional expression"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(1000);

        testExampleRun("Use a conditional expression", "20");
        testExampleRun("Null-checks", "6");
        testExampleRun("is-checks and automatic casts", "3\n" +
                "null");
        testExampleRun("Use a while-loop", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "guest4");
        testExampleRun("Use a for-loop", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "\n" +
                "guest1\n" +
                "guest2\n" +
                "guest3");
        testExampleRun("Use ranges and in", "OK\n" +
                "1 2 3 4 5 \n" +
                "Out: array has only 3 elements. x = 4\n" +
                "Yes: array contains aaa\n" +
                "No: array doesn't contains ddd");
        testExampleRun("Use when", "Greeting\n" +
                "One\n" +
                "Long\n" +
                "Not a string\n" +
                "Unknown");
    }

    public void testAllDifficultExamples() throws IOException, InterruptedException {
        isRunTested = true;
        driver.findElement(By.id("Longer examples")).click();
        final WebElement el = driver.findElement(By.id("Life"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(1000);

        testDifficultExampleRun("99 Bottles of Beer");
        testDifficultExampleRun("HTML Builder");
        testDifficultExampleRun("Life", true);
        testDifficultExampleRun("Maze", true);
    }

    public void testAllExamplesRunJs() throws IOException, InterruptedException {
        isRunTested = false;
        testExampleRun("Simplest version", "Hello, world!");
        testExampleRun("Reading a name from the command line", "Hello, guest!");
        testExampleRun("Reading many names from the command line", "Hello, guest1!\n" +
                "Hello, guest2!\n" +
                "Hello, guest3!");
        testExampleRun("A multi-language Hello", "Salut!");
        testExampleRun("An object-oriented Hello", "Hello, guest1");

        driver.findElement(By.id("Basic syntax walk-through")).click();
        final WebElement el = driver.findElement(By.id("Use a conditional expression"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(1000);

        testExampleRun("Use a conditional expression", "20");
        testExampleRun("Null-checks", "6");
        testExampleRun("is-checks and automatic casts", "3\n" +
                "null");
        testExampleRun("Use a while-loop", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "guest4");
        testExampleRun("Use a for-loop", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "\n" +
                "guest1\n" +
                "guest2\n" +
                "guest3");
        testExampleRun("Use ranges and in", "OK\n" +
                "1 2 3 4 5 \n" +
                "Out: array has only 3 elements. x = 4\n" +
                "Yes: array contains aaa\n" +
                "No: array doesn't contains ddd");
        testExampleRun("Use when", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Undefined descriptor: .java.lang.System.currentTimeMillis");

        driver.findElement(By.id("Longer examples")).click();
        final WebElement el2 = driver.findElement(By.id("Life"));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(1000);

        testExampleRun("99 Bottles of Beer", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "@NotNull method org/jetbrains/k2js/translate/reference/PropertyAccessTranslator.getGetterDescriptor must not return null");
        testExampleRun("HTML Builder", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Argument 0 for @NotNull parameter of org/jetbrains/k2js/translate/general/Translation.translateExpression must not be null");
        testExampleRun("Life", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "com.google.dart.compiler.backend.js.ast.JsIf cannot be cast to com.google.dart.compiler.backend.js.ast.JsExprStmt");
        testExampleRun("Maze", "The Pre-Alpha JavaScript back-end could not generate code for this program.\n" +
                "Try to run it using JVM.\n" +
                "Undefined descriptor: .java.util.HashMap.");
    }


    private String readResultFromFile(String filePath) throws IOException {
        return Files.toString(new File(TEST_SRC + File.separator + filePath), Charset.forName("UTF-8"));
    }


    private void testDifficultExampleRun(String name) throws IOException {
        testDifficultExampleRun(name, false);
    }

    private void testDifficultExampleRun(String name, boolean isSpaceReplaced) throws IOException {
        testExampleRun(name, readResultFromFile("examples" + File.separator + name + ".txt"), isSpaceReplaced);
    }

    private String getExampleNameByTestName() {
        String testName = this.getName();
        testName = ResponseUtils.substringAfter(testName, "testExample$");
        testName = testName.replace("$", " ");
        testName = testName.replace("MINUS", "-");
        return testName;
    }


    private void testExampleRun(final String exampleName, final String result) {
        testExampleRun(exampleName, result, false);
    }

    private void testExampleRun(final String exampleName, final String result, boolean isSpaceReplaced) {
        final WebElement example = accordion.findElement(By.id(exampleName));
        example.click();

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Example is loaded.") || statusBar.getText().equals("Can't get example from server."));
            }
        });

        if (isRunTested) {
            run.click();
        } else {
            runJs.click();
        }

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

        if (getName().endsWith("DifficultExamples")) {
            if (isSpaceReplaced) {
                testOneFileExampleWithReplaceSpacesAndLineSeparator(result);
            } else {
                testOneFileExampleWithReplaceLineSeparator(result);
            }
        } else {
            if (statusBar.getText().equals("Your program has terminated with an exception.")) {
                testErrorString(result);
            }   else {
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
