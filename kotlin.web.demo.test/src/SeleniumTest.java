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

// We specify the package of our tests

import com.google.common.io.Files;
import com.thoughtworks.selenium.CommandProcessor;
import junit.framework.TestCase;
import org.jetbrains.webdemo.ResponseUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// This is the driver's import. You'll use this for instantiating a
// browser and making it do what you need.

// Selenium-IDE add the Pattern module because it's sometimes used for
// regex validations. You can remove the module if it's not used in your
// script.

public class SeleniumTest extends TestCase {

    private final String TEST_SRC = "kotlin.web.demo.test/testData/";

    private WebDriver driver;
    private WebElement statusBar;
    private WebElement console;
    private WebElement run;
    private WebElement refresh;
    private WebElement selectmenu;
    private WebElement accordion;
    private Wait<WebDriver> wait;
    private WebElement editor;

    private CommandProcessor executor;

    private boolean isRunTested = true;

    public SeleniumTest(String name) {
        super(name);
    }

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
//        driver.get("http://kotlinsrv.labs.intellij.net");
        driver.get("http://localhost");
        statusBar = driver.findElement(By.id("statusbar"));
        console = driver.findElement(By.id("console"));
        run = driver.findElement(By.id("run"));
        selectmenu = driver.findElement(By.id("runConfigurationMode"));
        refresh = driver.findElement(By.id("refresh"));
        accordion = driver.findElement(By.id("examplesaccordion"));

        WebElement arguments = driver.findElement(By.id("arguments"));
        editor = driver.findElement(By.id("code"));
        WebElement problems = driver.findElement(By.id("code"));

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 500, 500);
    }

    private String generateIdFormNameAndFolder(String name, String folder) {
        return folder.replaceAll("([ ])", "_") + "&name=" + name.replaceAll("([ ])", "_");
    }

    public void testAllSimplesExamples() throws IOException, InterruptedException {
        isRunTested = true;

        driver.findElement(By.id("Hello,_world!")).click();
        final WebElement el = driver.findElement(By.id(generateIdFormNameAndFolder("Simplest version", "Hello, world!")));

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
        final WebElement el2 = driver.findElement(By.id(generateIdFormNameAndFolder("Use a conditional expression", "Basic_syntax_walk-through")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Use a conditional expression", "Basic_syntax_walk-through", "20");
        testExampleRun("Null-checks", "Basic_syntax_walk-through", "6");
        testExampleRun("is-checks_and_smart_casts", "Basic_syntax_walk-through", "3\n" +
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
        final WebElement el3 = driver.findElement(By.id(generateIdFormNameAndFolder("Life", "Longer_examples")));

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
        final WebElement el = driver.findElement(By.id(generateIdFormNameAndFolder("Simplest version", "Hello, world!")));

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
        final WebElement el2 = driver.findElement(By.id(generateIdFormNameAndFolder("Use a conditional expression", "Basic_syntax_walk-through")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("is-checks and smart casts", "Basic_syntax_walk-through", "3\n" +
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


    }

    public void testAllDifficultExamplesRunJs() throws IOException, InterruptedException {
        isRunTested = false;
        driver.findElement(By.id("Longer_examples")).click();
        final WebElement el3 = driver.findElement(By.id(generateIdFormNameAndFolder("Life", "Longer_examples")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el3.isDisplayed();
            }
        });

        Thread.sleep(500);

        testDifficultExampleRun("HTML Builder");
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
        Thread.sleep(1000);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //One error
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);

        //Two error in one line
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 2);

        //One warning
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out?.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        refresh.click();
        Thread.sleep(500);
        checkWarningElemements(1, 1);

        //One warning and one error
        js.executeScript("setEditorValue(\"" +
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
        js.executeScript("setEditorValue(\"" +
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
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);

        //Two error in one line
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 2);

        //One warning
        js.executeScript("setEditorValue(\"" +
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
        js.executeScript("setEditorValue(\"" +
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
        js.executeScript("setEditorValue(\"" +
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
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        js.executeScript("$(\"#runConfigurationMode\").selectmenu(\"value\", \"js\");");
        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 1);

        //Two error in one line
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        run.click();
        Thread.sleep(500);
        checkErrorsElemements(1, 2);

        //One warning
        js.executeScript("setEditorValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out?.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        run.click();
        Thread.sleep(500);
        checkWarningElemements(1, 1);
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Compilation competed successfully.") || statusBar.getText().equals("Your program has terminated with an exception."));
            }
        });
        testOutputString("Hello, world!");

        //One warning and one error
        js.executeScript("setEditorValue(\"" +
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
        js.executeScript("setEditorValue(\"" +
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
        List<WebElement> errors = problems.findElements(By.className("problemsViewWarningNeverUsed"));
//        List<WebElement> errors = problems.findElements(By.className("problemsViewWarning"));
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
        testExampleRun(name, "Longer_examples", readResultFromFile("execution" + File.separator + "txtExamples" + File.separator + name + ".txt"), isSpaceReplaced);
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
//        final WebElement example = accordion.findElement(By.id(exampleName.replaceAll("([ ])", "_") + "&folder=" + headName));
        final WebElement example = accordion.findElement(By.id(headName + "&name=" + exampleName.replaceAll("([ ])", "_")));
        example.click();

        Thread.sleep(500);

        /*wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Example is loaded.") || statusBar.getText().equals("Can't get example from server."));
            }
        });*/

        System.out.println(isRunTested);
        if (!isRunTested) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("changeConfiguration(\"js\");");
        }
        Thread.sleep(500);
        run.click();
        Thread.sleep(500);

        /*wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Running...") || statusBar.getText().equals("Your program has terminated with an exception."));
            }
        });*/

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Compilation competed successfully.")
                        || statusBar.getText().equals("Your program has terminated with an exception.")
                        || statusBar.getText().equals("Errors and warnings were loaded.")
                        || statusBar.getText().equals("Translation competed successfully."));
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
        if (expectedResult.contains("<html>")) {
            assertEquals(expectedResult, actualResult + System.getProperty("line.separator"));
        } else {
            assertEquals(expectedResult, actualResult);
        }
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
        if (expectedResult.contains("<html>")) {
            assertEquals(expectedResult, replaceLineSeparators(actualResult) + System.getProperty("line.separator"));
        } else {
            assertEquals(expectedResult, replaceLineSeparators(actualResult));
        }
    }

    private String replaceSpacesAndLineSeparators(String str) {
        return str.replaceAll("([ ])", "_").replaceAll("([\n])", System.getProperty("line.separator"));
    }

    private String replaceLineSeparators(String str) {
        return str.replaceAll("([\n])", System.getProperty("line.separator"));
    }

    /*public void testSaveProgram() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Thread.sleep(2000);
        //js.executeScript("setLogin();");

        WebElement el1 = driver.findElement(By.id("login")).findElements(By.tagName("img")).get(2);
        el1.click();
        Thread.sleep(1000);


        WebElement el = driver.findElement(By.id("saveProgram"));
        el.click();

        Thread.sleep(500);
        String programName = "test" + new Random().nextInt();
        js.executeScript("$('#programName').val('" + programName + "');");
        Thread.sleep(500);
        WebElement saveInDialog = driver.findElement(By.className("ui-button-text-only"));
        saveInDialog.click();
        Thread.sleep(500);
        assertEquals("Saved as: " + programName + ".", statusBar.getText());
        el.click();
        Thread.sleep(500);
        assertEquals("Your program was successfully saved.", statusBar.getText());

        WebElement publicLink = driver.findElement(By.id("myprogramscontent")).findElements(By.tagName("img")).get(1);
        publicLink.click();
        Thread.sleep(500);
        assertEquals("Public link was generated.", statusBar.getText());

    }*/


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        driver.quit();

    }


}
