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
import org.apache.commons.lang.math.RandomUtils;
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
        driver = new FirefoxDriver();
//        driver.get("http://kotlin-demo.jetbrains.com");
//        driver.get("http://kotlinsrv.labs.intellij.net");
        driver.get("http://localhost:8080/");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.clear()");

        statusBar = driver.findElement(By.id("statusBar"));

        console = driver.findElement(By.id("program-output"));
        run = driver.findElement(By.id("runButton"));
//        selectmenu = driver.findElement(By.id("runConfigurationMode"));
        accordion = driver.findElement(By.id("examples-list"));

//        WebElement arguments = driver.findElement(By.id("arguments"));
//        editor = driver.findElement(By.id("code"));
//        WebElement problems = driver.findElement(By.id("code"));

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 500, 500);
    }

    private String generateIdFormNameAndFolder(String name, String folder) {
        return "folder=" + folder.replaceAll(" ", "%20") + "&project=" + name.replaceAll(" ", "%20");
    }

    public void testAllSimplesExamples() throws IOException, InterruptedException {
        isRunTested = true;

        driver.findElement(By.id("Hello,%20world!")).click();
        final WebElement el = driver.findElement(By.id(generateIdFormNameAndFolder("Simplest version", "Hello, world!")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Simplest version", "Hello, world!", "Hello, world!");
        testExampleRun("Reading a name from the command line", "Hello, world!", "Hello, guest!");
        testExampleRun("Reading many names from the command line", "Hello, world!", "Hello, guest1!\n" +
                "Hello, guest2!\n" +
                "Hello, guest3!");
        testExampleRun("A multi-language Hello", "Hello, world!", "Salut!");
        testExampleRun("An object-oriented Hello", "Hello, world!", "Hello, guest1");

        driver.findElement(By.id("Basic%20syntax%20walk-through")).click();
        final WebElement el2 = driver.findElement(By.id(generateIdFormNameAndFolder("Use a conditional expression", "Basic syntax walk-through")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Use a conditional expression", "Basic syntax walk-through", "20");
        testExampleRun("Null-checks", "Basic syntax walk-through", "6");
        testExampleRun("is-checks and smart casts", "Basic syntax walk-through", "3\n" +
                "null");
        testExampleRun("Use a while-loop", "Basic syntax walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "guest4");
        testExampleRun("Use a for-loop", "Basic syntax walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "\n" +
                "guest1\n" +
                "guest2\n" +
                "guest3");
        testExampleRun("Use ranges and in", "Basic syntax walk-through", "OK\n" +
                "1 2 3 4 5 \n" +
                "Out: array has only 3 elements. x = 4\n" +
                "Yes: array contains aaa\n" +
                "No: array doesn't contains ddd");
        testExampleRun("Use when", "Basic syntax walk-through", "Greeting\n" +
                "One\n" +
                "Long\n" +
                "Not a string\n" +
                "Unknown");
    }

    public void testAllDifficultExamples() throws IOException, InterruptedException {
        isRunTested = true;
        driver.findElement(By.id("Longer%20examples")).click();
        final WebElement el3 = driver.findElement(By.id(generateIdFormNameAndFolder("Life", "Longer examples")));

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

        driver.findElement(By.id("Hello,%20world!")).click();
        final WebElement el = driver.findElement(By.id(generateIdFormNameAndFolder("Simplest version", "Hello, world!")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("Simplest version", "Hello, world!", "Hello, world!");
        testExampleRun("Reading a name from the command line", "Hello, world!", "Hello, guest!");
        testExampleRun("Reading many names from the command line", "Hello, world!", "Hello, guest1!\n" +
                "Hello, guest2!\n" +
                "Hello, guest3!");
        testExampleRun("A multi-language Hello", "Hello, world!", "Salut!");
        testExampleRun("An object-oriented Hello", "Hello, world!", "Hello, guest1");

        driver.findElement(By.id("Basic%20syntax%20walk-through")).click();
        final WebElement el2 = driver.findElement(By.id(generateIdFormNameAndFolder("Use a conditional expression", "Basic syntax walk-through")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el2.isDisplayed();
            }
        });

        Thread.sleep(500);

        testExampleRun("is-checks and smart casts", "Basic syntax walk-through", "3\n" +
                "null");
        testExampleRun("Use a while-loop", "Basic syntax walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "guest4");
        testExampleRun("Use a for-loop", "Basic syntax walk-through", "guest1\n" +
                "guest2\n" +
                "guest3\n" +
                "\n" +
                "guest1\n" +
                "guest2\n" +
                "guest3");


    }

    public void testAllDifficultExamplesRunJs() throws IOException, InterruptedException {
        isRunTested = false;
        driver.findElement(By.id("Longer%20examples")).click();
        final WebElement el3 = driver.findElement(By.id(generateIdFormNameAndFolder("Life", "Longer examples")));

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return el3.isDisplayed();
            }
        });

        Thread.sleep(500);

        testDifficultExampleRun("HTML Builder");
    }


    public void testAllSimplestExamplesRunWhenServerOn() throws IOException, InterruptedException {
        testAllSimplesExamples();
        testAllExamplesRunJs();
    }

    public void testAllDifficultExamplesRunWhenServerOn() throws IOException, InterruptedException {
        testAllDifficultExamples();
    }

    public void testErrorsAndWarningsOnTheFly() throws InterruptedException {
        Thread.sleep(1000);
        WebElement onTheFlyCheckBox = driver.findElement(By.id("on-the-fly-checkbox"));
        if (!onTheFlyCheckBox.isSelected()) {
            onTheFlyCheckBox.click();
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //One error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        checkErrorsElements(1, 1);

        //Two error in one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        checkErrorsElements(2, 1);

        //One warning
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        checkWarningElements(1, 1);

        //One warning and one error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        checkErrorsElements(1, 1);
        checkWarningElements(1, 1);

        //One warning and one error at one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\"); val a = 10\\n" +
                " val b = 10\\n" +
                "}" +
                "\");");

        Thread.sleep(500);
        checkErrorsElements(1, 1);
        checkWarningElements(2, 1);
    }

    public void testErrorsAndWarningsWithRunButton() throws InterruptedException {
        Thread.sleep(1000);
        WebElement onTheFlyCheckBox = driver.findElement(By.id("on-the-fly-checkbox"));
        if (onTheFlyCheckBox.isSelected()) {
            onTheFlyCheckBox.click();
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //One error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\")\\n" +
                "}" +
                "\");");
        run.click();
        Thread.sleep(500);
        checkErrorsElements(1, 1);

        //Two error in one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\"\\n" +
                "}" +
                "\");");
        run.click();
        Thread.sleep(1000);
        checkErrorsElements(2, 1);

        //One warning
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.println(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");
        run.click();
        Thread.sleep(500);
        checkWarningElements(1, 1);

        //One warning and one error
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\")\\n" +
                " val a = 10\\n" +
                "}" +
                "\");");
        run.click();
        Thread.sleep(500);
        checkErrorsElements(1, 1);
        checkWarningElements(1, 1);

        //One warning and one error at one line
        js.executeScript("editor.setValue(\"" +
                "fun main(args : Array<String>) {\\n" +
                " System.out.prntln(\\\"Hello, world!\\\"); val a = 10\\n" +
                " val b = 10\\n" +
                "}" +
                "\");");
        run.click();
        Thread.sleep(500);
        checkErrorsElements(1, 1);
        checkWarningElements(2, 1);
    }

    public void testErrorsAndWarningsWithRunJsButton() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("configurationManager.updateConfiguration(\"js\");");
        testErrorsAndWarningsWithRunButton();
    }

    public void testErrorsAndWarningsWhenServerOn() throws IOException, InterruptedException {
        testErrorsAndWarningsOnTheFly();
        testErrorsAndWarningsWithRunButton();
        testErrorsAndWarningsWithRunJsButton();
    }

    public int getNumberOfErrorGutters() {
        WebElement problems = driver.findElement(By.id("editorinput"));
        List<WebElement> errors = problems.findElements(By.className("ERRORgutter"));
        return errors.size();
    }

    public int getNumberOfErrorsInProblemView() {
        WebElement problems = driver.findElement(By.id("problems"));
        List<WebElement> errors = problems.findElements(By.cssSelector(".img.ERROR"));
        return errors.size();
    }

    public void checkErrorsElements(int problemView, int errorGutter) {
        assertEquals(problemView, getNumberOfErrorsInProblemView());
        assertEquals(errorGutter, getNumberOfErrorGutters());
    }

    public int getNumberOfWarningGutters() {
        WebElement problems = driver.findElement(By.id("editorinput"));
        List<WebElement> errors = problems.findElements(By.className("WARNINGgutter"));
        return errors.size();
    }

    public int getNumberOfWarningInProblemView() {
        WebElement problems = driver.findElement(By.id("problems"));
        List<WebElement> errors = problems.findElements(By.cssSelector(".img.WARNING"));
        return errors.size();
    }

    public void checkWarningElements(int problemView, int errorGutter) {
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
        String result;
        result = readResultFromFile("execution" + File.separator + "txtExamples" + File.separator + name + ".txt");
        result = result.replaceAll("\\r", "");
        testExampleRun(name, "Longer examples", result, isSpaceReplaced);
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

    private void testExampleRun(final String exampleName, final String folderName, final String result, boolean isSpaceReplaced) throws InterruptedException {
//        final WebElement example = accordion.findElement(By.id(exampleName.replaceAll("([ ])", "_") + "&folder=" + headName));
        final WebElement example = accordion.findElement(By.id(generateIdFormNameAndFolder(exampleName, folderName)));
        example.click();

        Thread.sleep(500);

        /*wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (statusBar.getText().equals("Example is loaded.") || statusBar.getText().equals("Can't get example from server."));
            }
        });*/

        if (!isRunTested) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("configurationManager.updateConfiguration(\"js\");");
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

        Thread.sleep(500);

        assertEquals(result, getStandardOutput());
//        if (getName().contains("DifficultExamples")) {
//            if (isSpaceReplaced) {
//                testOneFileExampleWithReplaceSpacesAndLineSeparator(result);
//            } else {
//                testOneFileExampleWithReplaceLineSeparator(result);
//            }
//        } else {
//            if (statusBar.getText().equals("Your program has terminated with an exception.")) {
//                testErrorString(result);
//            } else {
//                testOutputString(result);
//            }
//        }
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

    private String getStandardOutput() {
        StringBuilder actualResult = new StringBuilder();
        List<WebElement> list = console.findElements(By.className("standard-output"));
        for (WebElement webElement : list) {
            actualResult.append(webElement.getText());
        }
        return actualResult.toString();
    }

    private String replaceSpacesAndLineSeparators(String str) {
        return str.replaceAll("([ ])", "_").replaceAll("([\n])", System.getProperty("line.separator"));
    }

    private String replaceLineSeparators(String str) {
        return str.replaceAll("([\n])", System.getProperty("line.separator"));
    }

    public void testLogin() throws InterruptedException {
        login();
    }

    private void login() throws InterruptedException {
        WebElement element = driver.findElement(By.id("login-with-google"));
        element.click();
        try {
            element = driver.findElement(By.id("Email"));
            final WebElement finalElement = element;
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return finalElement.isDisplayed();
                }
            });
            element.sendKeys("kotlin.web.demo.test");
            element = driver.findElement(By.id("Passwd"));
            element.sendKeys("kotlinwebdemo");
            element = driver.findElement(By.id("signIn"));
            element.click();
            element = driver.findElement(By.id("submit_approve_access"));
            final WebElement finalElement1 = element;
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return finalElement1.isEnabled();
                }
            });
            element.click();
        } catch (NoSuchElementException e) {
            /*If it's not our first authentication.*/
        }

        element = driver.findElement(By.id("userNameTitle"));
        assertEquals(element.getText(), "Natalia Ukhorskaya");
    }

//    public void testWorkForLoggedInUser() throws InterruptedException {
//        login();
//        int countOfProgramsInListBefore = driver.findElement(By.id("My_Programs_content")).findElements(By.className("examples-project-name")).size();
//        createProgram();
//        Thread.sleep(500);
//        int countOfProgramsInListAfter = driver.findElement(By.id("My_Programs_content")).findElements(By.className("examples-project-name")).size();
//        assertEquals(countOfProgramsInListBefore + 1, countOfProgramsInListAfter);
//
//        createProgram();
//
//        countOfProgramsInListBefore = driver.findElement(By.id("myprogramscontent")).findElements(By.tagName("table")).size();
//        WebElement tableForProgram = driver.findElement(By.id("myprogramscontent")).findElements(By.tagName("table")).get(countOfProgramsInListBefore - 1);
//        String elementId = tableForProgram.findElement(By.tagName("a")).getAttribute("id");
//
//        WebElement element = driver.findElement(By.xpath("//table[tr/td/a[@id=\"" + elementId + "\"]]/tr/td/img[@src=\"/static/icons/link1.png\"]"));
//        element.click();
//
//        assert driver.findElement(By.xpath("//div[@id=\"pld" + elementId + "\"]/div/div/a[img[contains(@src, \"twitter\")]]")).isDisplayed();
//        assert driver.findElement(By.id("pld" + elementId)).isDisplayed();
//        WebElement closePublicLinkButton = driver.findElement(By.xpath("//div[@id=\"pld" + elementId + "\"]/div/div/img[contains(@src, \"close\")]"));
//        assert closePublicLinkButton.isDisplayed();
//        closePublicLinkButton.click();
//
//        assert !driver.findElement(By.xpath("//div[@id=\"pld" + elementId + "\"]/div/div/a[img[contains(@src, \"twitter\")]]")).isDisplayed();
//        assert !driver.findElement(By.id("pld" + elementId)).isDisplayed();
//        assert !closePublicLinkButton.isDisplayed();
//
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//
//
//        js.executeScript("setEditorValue(\"" +
//                "fun main(args : Array<String>) {\\n" +
//                "  println(\\\"Test message!\\\")\\n" +
//                "}" +
//                "\");");
//
//        element = driver.findElement(By.id("saveProgram"));
//        element.click();
//        Thread.sleep(500);
//        checkEditorValue("fun main(args : Array<String>) {\n" +
//                "  println(\"Test message!\")\n" +
//                "}");
//
//        driver.findElement(By.id("myprogramscontent")).findElements(By.tagName("table")).get(countOfProgramsInListBefore - 2).findElement(By.tagName("a")).click();
//        Thread.sleep(500);
//        checkEditorValue("fun main(args : Array<String>) {\n" +
//                "  println(\"Hello, world!\")\n" +
//                "}\n");
//        Thread.sleep(500);
//
//        driver.findElement(By.id("myprogramscontent")).findElements(By.tagName("table")).get(countOfProgramsInListBefore - 1).findElement(By.tagName("a")).click();
//
//        checkEditorValue("fun main(args : Array<String>) {\n" +
//                "  println(\"Test message!\")\n" +
//                "}");
//
//        element = driver.findElement(By.xpath("//table[tr/td/a[@id=\"" + elementId + "\"]]/tr/td/img[@src=\"/static/icons/delete.png\"]"));
//        element.click();
//
//
//        Alert alert = driver.switchTo().alert();
//        alert.accept();
//        Thread.sleep(500);
//        countOfProgramsInListAfter = driver.findElement(By.id("myprogramscontent")).findElements(By.tagName("table")).size();
//        assertEquals(countOfProgramsInListBefore - 1, countOfProgramsInListAfter);
//    }

    private void checkEditorValue(String expectedResult) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String editorValue;
        Alert alertWindow;
        editorValue = (String) js.executeScript("alert(getEditorValue());");
        assertEquals(expectedResult, editorValue);
        alertWindow = driver.switchTo().alert();
        alertWindow.accept();
    }

    private void createProgram() {
        WebElement element = driver.findElement(By.id("My_Programs"));
        element.click();
        element = driver.findElement(By.id("saveAsProgram"));
        element.click();
        element = driver.findElement(By.id("programName"));
        final WebElement finalElement = element;
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return finalElement.isDisplayed();
            }
        });
        String testName = "test" + RandomUtils.nextInt();
        element.sendKeys(testName);
        element = driver.findElement(By.xpath("//button[span[text()=\"Save\"]]"));
        element.click();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        driver.quit();

    }


}
