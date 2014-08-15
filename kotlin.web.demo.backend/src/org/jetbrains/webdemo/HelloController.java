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

package org.jetbrains.webdemo;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
 @RequestMapping("/mylnikov")
 public class HelloController {


    protected Project currentProject;
    protected PsiFile currentPsiFile;
    //public static EnvironmentManager environmentManager = new EnvironmentManagerForServer();
    private int x;
    private final ServerHandler myHandler = new ServerHandler();
    public HelloController()
    {
        x=5;
    }
   // public static Env envspace = new Env();
    /*
    Handling of compilation reqest
     */
    @RequestMapping( method=RequestMethod.POST, value="/compile")
    @ResponseBody public RequestIn ResponseOut(@RequestBody final RequestIn in) {


        //environmentManager.createEnvironment();
       // ServerInitializer.setEnvironmentManager(environmentManager);
        //Initializer.reinitializeJavaEnvironment();
        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
       // ServerInitializer.getInstance().initJavaCoreEnvironment();

        //currentPsiFile = JetPsiFactoryUtil.createFile(currentProject, in.getText());

       /*  try {
            if (ServerInitializer.getInstance().initJavaCoreEnvironment()) {
                ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                WebDemoTranslatorFacade.LOAD_JS_LIBRARY_CONFIG = new WebDemoConfigServer(Initializer.INITIALIZER.getEnvironment().getProject());
                ExamplesList.getInstance();
                HelpLoader.getInstance();
                Statistics.getInstance();
                //MySqlConnector.getInstance();
            } else {
                ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
            }
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }


        System.out.println(in.getConsoleArgs());

        ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
       // ServerInitializer.getInstance().initJavaCoreEnvironment();
        try {
            currentProject = Initializer.INITIALIZER.getEnvironment().getProject();

            currentPsiFile = JetPsiFactoryUtil.createFile(currentProject, in.getText());

            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(currentPsiFile, in.getConsoleArgs(), new SessionInfo("9874674", SessionInfo.TypeOfRequest.RUN));
            System.out.println(responseForCompilation.getResult());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //System.out.println(responseForCompilation.getResult());
//        ArrayNode jsonArray = new ArrayNode(JsonNodeFactory.instance);
//        org.codehaus.jackson.node.ObjectNode jsonObject = jsonArray.addObject();
//
//        jsonObject.put("text", in.getText());
//        jsonObject.put("consoleArgs", in.getConsoleArgs());
*/
        return in;
    }


}

