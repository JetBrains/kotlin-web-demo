package web.view.ukhorskaya.sessions;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.psi.PsiFile;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.math.RandomUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import web.view.ukhorskaya.Initializer;
import web.view.ukhorskaya.KotlinHttpServer;
import web.view.ukhorskaya.handlers.BaseHandler;
import web.view.ukhorskaya.responseHelpers.JsonResponseForCompletion;
import web.view.ukhorskaya.responseHelpers.JsonResponseForHighlighting;
import web.view.ukhorskaya.responseHelpers.ResponseForCompilation;

import java.awt.event.InputEvent;
import java.io.*;
import java.net.URLDecoder;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 9/6/11
 * Time: 12:14 PM
 */

public class HttpSession {
    private static final Logger LOG = Logger.getInstance(HttpSession.class);

    private static final KeyModifier SHIFT = new KeyModifier(InputEvent.SHIFT_DOWN_MASK + InputEvent.SHIFT_MASK, 16);
    private static final KeyModifier CTRL = new KeyModifier(InputEvent.CTRL_DOWN_MASK + InputEvent.CTRL_MASK, 17);
    private static final KeyModifier ALT = new KeyModifier(InputEvent.ALT_DOWN_MASK + InputEvent.ALT_MASK, 18);
    private static final KeyModifier META = new KeyModifier(InputEvent.META_DOWN_MASK + InputEvent.META_MASK, 19);

    protected Project currentProject;
    protected PsiFile currentPsiFile;
    protected Document currentDocument;

    private HttpExchange exchange;

    private final long startTime;

    public HttpSession(long startTime) {
        this.startTime = startTime;
    }

    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        String param = exchange.getRequestURI().toString();
        if (param.contains("testConnection")) {
            sendTestConnection();
            return;
        }
        if (param.contains("path=")) {
            if (param.contains("compile=true") || param.contains("run=true")) {
                sendExecutorResult();
                return;
            } else if (param.contains("complete=true")) {
                sendCompletionResult();
                return;
            } else if (param.contains("stop=true")) {
                stopSession();
                return;
            } else {
                sendProjectSourceFile();
                return;
            }
        }

        writeResponse("Wrong request: " + exchange.getRequestURI().toString(), HttpStatus.SC_NOT_FOUND, true);
    }

    private void sendTestConnection() {
        if (exchange.getRequestURI().toString().contains("stopTest=true")) {
            try {
                String response = getTextFromPostRequest();
                File file = new File("C:/Development/testData/testConnection" + System.currentTimeMillis() + ".csv");
                file.createNewFile();
                //System.out.println("RESULT: " + response);
                FileWriter writer = new FileWriter(file);
                writer.write(response);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            writeResponse("Response sended", HttpStatus.SC_OK, true);
        } else {
            StringBuilder response = new StringBuilder();
            OutputStream os = null;
            String path = "/testConnection.html";
            InputStream is = BaseHandler.class.getResourceAsStream(path);
            InputStreamReader reader = new InputStreamReader(is);

            try {
                BufferedReader bufferedReader = new BufferedReader(reader);

                String tmp;
                while ((tmp = bufferedReader.readLine()) != null) {
                    response.append(tmp);
                }
            } catch (NullPointerException e) {
                response.append("Resource file not found");
                writeResponse(response.toString(), HttpStatus.SC_NOT_FOUND);
            } catch (IOException e) {
                response.append("Error while reading from file in writeResponse()");
                writeResponse(response.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } catch (RuntimeException e) {
                writeResponse(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }

            try {
                exchange.sendResponseHeaders(400, response.toString().length());
                os = exchange.getResponseBody();
                os.write(response.toString().getBytes());
                //System.out.println("end writeResponse() = " + (System.currentTimeMillis() - startTime));
            } catch (IOException e) {
                //This is an exception we can't send data to client
            } catch (Throwable e) {
                //For do not stop server in all cases
                LOG.error(e);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
    }

    private void sendConsoleInputResponse() {

    }

    private void stopSession() {
        KotlinHttpServer.stopServer();

        FileDocumentManager.getInstance().saveAllDocuments();

        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project openProject : openProjects) {
            ProjectEx project = (ProjectEx) openProject;
            project.save();
        }

        System.exit(0);
    }

    private void setGlobalVariables(@Nullable String text) {
        String requestURI = exchange.getRequestURI().getPath().substring(6);
        requestURI = requestURI.replace("%20", " ");
        currentProject = Initializer.getEnvironment().getProject();
        if (text == null) {
            text = "namespace demo\n" +
                    "\n" +
                    "class Main() {\n" +
                    "\n" +
                    "  fun aaa() : Int {return 10}\n" +
                    "}\n" +
                    "\n" +
                    "fun main(args : Array<String>) {\n" +
                    "  var s = \"Natalia\";\n" +
                    "    System.out?.println(\"Hello, \" + s) \n" +
                    "    System.err?.println(\"ERROR\"); \n" +
                    "}  ";
        }

        currentPsiFile = JetPsiFactory.createFile(currentProject, text);
        currentDocument = currentPsiFile.getViewProvider().getDocument();
    }


    private void sendCompletionResult() {

        String param = exchange.getRequestURI().getQuery();
        String[] position = new String[0];
        if (param.contains("cursorAt")) {
            position = (param.substring(param.indexOf("cursorAt=") + 9)).split(",");
        }

        setGlobalVariables(getTextFromPostRequest());

        JsonResponseForCompletion jsonResponseForCompletion = new JsonResponseForCompletion(Integer.parseInt(position[0]), Integer.parseInt(position[1]), currentPsiFile);
        writeResponse(jsonResponseForCompletion.getResult(), HttpStatus.SC_OK, true);
    }

    private void sendProjectSourceFile() {
        String param = exchange.getRequestURI().getQuery();

        if ((param != null) && (param.contains("sendData=true"))) {
            setGlobalVariables(getTextFromPostRequest());
            //System.out.print("3 " + (System.currentTimeMillis() - startTime) + " ");
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile);
            String response = responseForHighlighting.getResult();
            //System.out.print("4 " + (System.currentTimeMillis() - startTime) + " ");
            response = response.replaceAll("\\n", "");
            writeResponse(response, HttpStatus.SC_OK, true);
        } else {
            setGlobalVariables(null);
            writeResponse(currentPsiFile.getText(), HttpStatus.SC_OK);
        }
    }

    private String getTextFromPostRequest() {
        StringBuilder reqResponse = new StringBuilder();
        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Impossible to write to file in UTF-8");
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(reader);

            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                reqResponse.append(tmp);
            }
        } catch (NullPointerException e) {
            reqResponse.append("Resource file not found");
            writeResponse(reqResponse.toString(), HttpStatus.SC_NOT_FOUND);
        } catch (IOException e) {
            reqResponse.append("Error while reading from file sendProjectSourceFile()");
            writeResponse(reqResponse.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        String finalResponse = null;
        try {
            finalResponse = URLDecoder.decode(reqResponse.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Impossible to write to file in UTF-8");
        }

        finalResponse = finalResponse.replaceAll("<br>", "\n");

        if (finalResponse.length() > 5) {
            return finalResponse.substring(5);
        } else {
            writeResponse("Post request is too short", HttpStatus.SC_BAD_REQUEST);
        }
        return "";
    }

    private void sendExecutorResult() {
        setGlobalVariables(getTextFromPostRequest());

        if (exchange.getRequestURI().getQuery().contains("compile")) {
            compileOrRunProject(true);
        } else if (exchange.getRequestURI().getQuery().contains("run")) {
            compileOrRunProject(false);
        } else {
            writeResponse("Incorrect url: absent run or compile parameter", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void compileOrRunProject(final boolean isOnlyCompilation) {
        ResponseForCompilation responseForCompilation = new ResponseForCompilation(isOnlyCompilation, currentPsiFile);
        writeResponse(responseForCompilation.getResult(), HttpStatus.SC_OK, true);
    }

    private void writeResponse(String responseBody, int errorCode) {
        writeResponse(responseBody, errorCode, false);
    }

    //Send Response
    //disableHeaders - disable html header for answer
    private void writeResponse(String responseBody, int errorCode, boolean disableHeaders) {
        //EditorFactoryImpl.getInstance().releaseEditor(currentEditor);
        OutputStream os = null;
        StringBuilder response = new StringBuilder();

        String path;
        if (!disableHeaders) {
            path = "/header.html";
            InputStream is = BaseHandler.class.getResourceAsStream(path);
            if (is == null) {
                writeResponse("File not found", HttpStatus.SC_NOT_FOUND);
                return;
            }
            InputStreamReader reader = new InputStreamReader(is);

            try {
                BufferedReader bufferedReader = new BufferedReader(reader);

                String tmp;
                while ((tmp = bufferedReader.readLine()) != null) {
                    response.append(tmp);
                }
            } catch (NullPointerException e) {
                response.append("Resource file not found");
                writeResponse(response.toString(), HttpStatus.SC_NOT_FOUND);
            } catch (IOException e) {
                response.append("Error while reading from file in writeResponse()");
                writeResponse(response.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } catch (RuntimeException e) {
                writeResponse(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.append("RESPONSEBODY");
        }

        //ShortcutSet gotofile = ActionManager.getInstance().getAction("GotoFile").getShortcutSet();
        //ShortcutSet gotoclass = ActionManager.getInstance().getAction("GotoClass").getShortcutSet();
        //ShortcutSet gotosymbol = ActionManager.getInstance().getAction("GotoSymbol").getShortcutSet();

        String finalResponse = response.toString();
        //finalResponse = finalResponse.replaceFirst("GOTOFILESHORTCUT", getKeyboardShortcutFromShortcutSet(gotofile));
        //finalResponse = finalResponse.replaceFirst("GOTOCLASSSHORTCUT", getKeyboardShortcutFromShortcutSet(gotoclass));
        //finalResponse = finalResponse.replaceFirst("GOTOSYMBOLSHORTCUT", getKeyboardShortcutFromShortcutSet(gotosymbol));

        //if (currentProject != null) {
        //   finalResponse = finalResponse.replaceFirst("PROJECTNAME", currentProject.getName());
        //}

        //finalResponse = finalResponse.replaceFirst("GOTOFILESHORTCUTSTRING", gotofile.getShortcuts()[0].toString());
        // finalResponse = finalResponse.replaceFirst("GOTOCLASSSHORTCUTSTRING", gotoclass.getShortcuts()[0].toString());
        // finalResponse = finalResponse.replaceFirst("GOTOSYMBOLSHORTCUTSTRING", gotosymbol.getShortcuts()[0].toString());

        finalResponse = finalResponse.replace("RESPONSEBODY", responseBody);

        try {
            if (exchange.getRequestURI().toString().contains("&time=")) {
                String query = exchange.getRequestURI().getQuery();
                query = query.substring(query.indexOf("&time=") + 6);
                exchange.getResponseHeaders().add("time", query);
            }
            exchange.sendResponseHeaders(errorCode, finalResponse.length());
            os = exchange.getResponseBody();
            os.write(finalResponse.getBytes());
            //long time = (System.currentTimeMillis() - startTime);
            //if (time == 0) {
            //     System.out.println(startTime + " " + System.currentTimeMillis() + " " + finalResponse);
            // }
            //long timeDiff = System.currentTimeMillis() - startTime;
            //System.out.println(exchange.getRequestURI().toString() + " " + timeDiff + " " + (timeDiff / 1000000) + ";");
            System.out.println((System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            //This is an exception we can't send data to client
        } catch (Throwable e) {
            //For do not stop server in all cases
            LOG.error(e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //LOG.error(e);
            }
        }

    }

    private String getKeyboardShortcutFromShortcutSet(ShortcutSet set) {
        StringBuilder result = new StringBuilder();
        int modifiers = ((KeyboardShortcut) (set.getShortcuts()[0])).getFirstKeyStroke().getModifiers();
        result.append(setModifiers(modifiers));
        int keyCode = ((KeyboardShortcut) (set.getShortcuts()[0])).getFirstKeyStroke().getKeyCode();
        result.append(keyCode);
        return result.toString();
    }

    private String setModifiers(int modifiers) {
        String result = "";
        if (modifiers == SHIFT.modifier) {
            result += SHIFT.key + ",";
        } else if (modifiers == CTRL.modifier) {
            result += CTRL.key + ",";
        } else if (modifiers == ALT.modifier) {
            result += ALT.key + ",";
        } else if (modifiers == META.modifier) {
            result += META.key + ",";
        } else if (modifiers == SHIFT.modifier + CTRL.modifier) {
            result += SHIFT.key + "," + CTRL.key + ",";
        } else if (modifiers == SHIFT.modifier + ALT.modifier) {
            result += SHIFT.key + "," + ALT.key;
        } else if (modifiers == CTRL.modifier + ALT.modifier) {
            result += CTRL.key + "," + ALT.key + ",";
        } else if (modifiers == SHIFT.modifier + ALT.modifier + CTRL.modifier) {
            result += SHIFT.key + "," + ALT.key + "," + CTRL.key + ",";
        } else if (modifiers == SHIFT.modifier + META.modifier) {
            result += SHIFT.key + "," + META.key;
        } else if (modifiers == CTRL.modifier + META.modifier) {
            result += CTRL.key + "," + META.key + ",";
        } else if (modifiers == SHIFT.modifier + META.modifier + CTRL.modifier) {
            result += SHIFT.key + "," + META.key + "," + CTRL.key + ",";
        } else if (modifiers != 0) {
            LOG.error("Error: there isn't a value for modifiers: " + modifiers);
        }
        return result;
    }


    private static class KeyModifier {
        public final int modifier;
        public final int key;

        private KeyModifier(int modifier, int key) {
            this.modifier = modifier;
            this.key = key;
        }
    }


}
