package web.view.ukhorskaya.responseHelpers;

import com.google.common.base.Predicates;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.apache.log4j.Logger;
import org.jetbrains.jet.codegen.ClassBuilderFactory;
import org.jetbrains.jet.codegen.ClassFileFactory;
import org.jetbrains.jet.codegen.GenerationState;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetNamespace;
import org.jetbrains.jet.lang.resolve.AnalyzingUtils;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JavaDefaultImports;
import org.json.*;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.errorsDescriptors.ErrorAnalyzer;
import web.view.ukhorskaya.errorsDescriptors.ErrorDescriptor;
import web.view.ukhorskaya.server.ServerSettings;
import web.view.ukhorskaya.sessions.HttpSession;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/20/11
 * Time: 1:41 PM
 */

public class CompileAndRunExecutor {
    private final Logger LOG = Logger.getLogger(CompileAndRunExecutor.class);

    private final boolean isOnlyCompilation;
    private final PsiFile currentPsiFile;
    private final String arguments;

    public CompileAndRunExecutor(boolean onlyCompilation, PsiFile currentPsiFile, String arguments) {
        this.isOnlyCompilation = onlyCompilation;
        this.currentPsiFile = currentPsiFile;
        this.arguments = arguments;
    }

    public String getResult() {
           ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile);
        List<ErrorDescriptor> errors = analyzer.getAllErrors();
        if (errors == null) {
            return ResponseUtils.getErrorInJson("Exception in Kotlin CORE: bug was reported to developers.");
        }
        if (errors.isEmpty() || isOnlyWarnings(errors)) {
            if (isOnlyCompilation) {
                return "[{\"text\":\"Compilation complete successfully\",\"type\":\"out\"}]";
            }
            Project currentProject = currentPsiFile.getProject();
            List<JetNamespace> namespaces = Collections.singletonList(((JetFile) currentPsiFile).getRootNamespace());
            BindingContext bindingContext = AnalyzingUtils.getInstance(JavaDefaultImports.JAVA_DEFAULT_IMPORTS).analyzeNamespaces(
                    currentProject,
                    namespaces,
                    Predicates.<PsiFile>equalTo(currentPsiFile),
                    JetControlFlowDataTraceFactory.EMPTY);

            HttpSession.TIME_MANAGER.saveCurrentTime();
            GenerationState generationState = new GenerationState(currentProject, ClassBuilderFactory.BINARIES);
            generationState.compileCorrectNamespaces(bindingContext, namespaces);
            LOG.info("userId=" + HttpSession.SESSION_ID + " COMPILE correctNamespaces " + HttpSession.TIME_MANAGER.getMillisecondsFromSavedTime());

            StringBuilder stringBuilder = new StringBuilder("Generated classfiles: ");
            stringBuilder.append(ResponseUtils.addNewLine());
            final ClassFileFactory factory = generationState.getFactory();

            HttpSession.TIME_MANAGER.saveCurrentTime();
            List<String> files = factory.files();
            for (String file : files) {
                File outputDir = new File(ServerSettings.OUTPUT_DIRECTORY);
                ServerSettings.OUTPUT_DIRECTORY = outputDir.getAbsolutePath();
                boolean isOutputExists = true;
                if (!outputDir.exists()) {
                    isOutputExists = outputDir.mkdirs();
                }
                if (isOutputExists) {
                    File target = new File(ServerSettings.OUTPUT_DIRECTORY, file);
                    try {
                        FileUtil.writeToFile(target, factory.asBytes(file));
                        stringBuilder.append(file).append(ResponseUtils.addNewLine());
                    } catch (IOException e) {
                        ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog("execute", "Cannot save file on disk. " + e.getMessage(), currentPsiFile.getText()), e);
                        return ResponseUtils.getErrorInJson("Cannot get a completion.");
                    }
                }    else {
                    ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog("execute", "Cannot create output directory for files: " + outputDir.getAbsolutePath(), currentPsiFile.getText()));
                    return ResponseUtils.getErrorInJson("Error on server: cannot run your program.");
                }

            }
            LOG.info("userId=" + HttpSession.SESSION_ID + " Write files on disk " + HttpSession.TIME_MANAGER.getMillisecondsFromSavedTime());

            JSONArray jsonArray = new JSONArray();
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", "info");
            map.put("text", stringBuilder.toString());
            jsonArray.put(map);

            JavaRunner runner = new JavaRunner(files, arguments, jsonArray, currentPsiFile.getText());

            return runner.getResult();

        } else {
            return generateResponseWithErrors(errors);
        }
    }

    private boolean isOnlyWarnings(List<ErrorDescriptor> list) {
        for (ErrorDescriptor errorDescriptor : list) {
            if (errorDescriptor.getSeverity() == Severity.ERROR) {
                return false;
            }
        }
        return true;
    }

    private String generateResponseWithErrors(List<ErrorDescriptor> errors) {
        JSONArray jsonArray = new JSONArray();

        for (ErrorDescriptor error : errors) {
            StringBuilder message = new StringBuilder();
            message.append("(").append(error.getInterval().startPoint.line + 1).append(", ").append(error.getInterval().startPoint.charNumber + 1).append(")");
            message.append(" - ");
            message.append(error.getMessage());
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", error.getSeverity().name());
            map.put("message", message.toString());
            jsonArray.put(map);
        }

        return jsonArray.toString();
    }

}
