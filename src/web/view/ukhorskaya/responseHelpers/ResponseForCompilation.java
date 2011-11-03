package web.view.ukhorskaya.responseHelpers;

import com.google.common.base.Predicates;
import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.jet.codegen.ClassFileFactory;
import org.jetbrains.jet.codegen.GenerationState;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetNamespace;
import org.jetbrains.jet.lang.resolve.AnalyzingUtils;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JavaDefaultImports;
import org.json.JSONArray;
import web.view.ukhorskaya.JavaExecuteSecurityManager;
import web.view.ukhorskaya.JavaRunner;
import web.view.ukhorskaya.errorsDescriptors.ErrorAnalyzer;
import web.view.ukhorskaya.errorsDescriptors.ErrorDescriptor;

import java.io.File;
import java.io.IOException;
import java.security.Policy;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/20/11
 * Time: 1:41 PM
 */

public class ResponseForCompilation {

    private final boolean isOnlyCompilation;
    private final PsiFile currentPsiFile;

    private String finalResult;

    public ResponseForCompilation(boolean onlyCompilation, PsiFile currentPsiFile) {
        this.isOnlyCompilation = onlyCompilation;
        this.currentPsiFile = currentPsiFile;
    }

    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile);
        List<ErrorDescriptor> errors = analyzer.getAllErrors();
        if (errors.isEmpty() || isOnlyWarnings(errors)) {
            if (isOnlyCompilation) {
                return "Compilation complete successfully";
            }
            Project currentProject = currentPsiFile.getProject();
            List<JetNamespace> namespaces = Collections.singletonList(((JetFile) currentPsiFile).getRootNamespace());
            BindingContext bindingContext = AnalyzingUtils.getInstance(JavaDefaultImports.JAVA_DEFAULT_IMPORTS).analyzeNamespaces(
                    currentProject,
                    namespaces,
                    Predicates.<PsiFile>equalTo(currentPsiFile),
                    JetControlFlowDataTraceFactory.EMPTY);
            GenerationState generationState = new GenerationState(currentProject, false);
            generationState.compileCorrectNamespaces(bindingContext, namespaces);

            /*SecurityManager securityManager = System.getSecurityManager();
            if (securityManager == null) {
                securityManager = new JavaExecuteSecurityManager();
                System.setSecurityManager(securityManager);
            }*/
            

            final ClassFileFactory factory = generationState.getFactory();
            List<String> files = factory.files();

            StringBuilder stringBuilder = new StringBuilder("Generated classfiles: <br>");
            for (String file : files) {
                //TODO setup tmp directory
                File target = new File(JavaRunner.OUTPUT_DIRECTORY, file);
               // try {
                    try {

                        //securityManager.checkWrite(target.getAbsolutePath());
                        FileUtil.writeToFile(target, factory.asBytes(file));
                        stringBuilder.append(file).append("<br>");
                    } catch (Exception e) {
                        stringBuilder.append("You doesn't have an access to write in ").append(target.getAbsolutePath()).append("<br>");
                        System.out.println("You doesn't have an access to write in" + target.getAbsolutePath());
                        return stringBuilder.toString();
                    }

                /*} catch (IOException e) {
                    stringBuilder.append("Impossible to generate classfile");
                }*/
            }

            JavaRunner runner = new JavaRunner(files);
            stringBuilder.append("<br>");
            stringBuilder.append(runner.run());

            return stringBuilder.toString();

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
        final StringBuilder builder = new StringBuilder();
        for (ErrorDescriptor error : errors) {
            SeverityHtmlDescriptor htmlSeverity = new SeverityHtmlDescriptor(error.getSeverity());
            builder.append("<p class=\"newLineClass\">").append("<img src=\"" + htmlSeverity.icon + "\"/>");
            builder.append(error.getSeverity().name());
            builder.append(": <font color=\"" + htmlSeverity.fontColor + "\">");
            builder.append("(").append(error.getInterval().startPoint.line + 1).append(", ").append(error.getInterval().startPoint.charNumber + 1).append(")");
            builder.append(" - ");
            builder.append(error.getMessage());
            builder.append("</font></p>");

        }

        return builder.toString();
    }

    private class SeverityHtmlDescriptor {
        public String fontColor;
        public String icon;

        private SeverityHtmlDescriptor(Severity severity) {
            if (severity == Severity.ERROR) {
                fontColor = "red";
                icon = "/icons/error.png";
            } else if (severity == Severity.INFO) {
                fontColor = "blue";
                icon = "/icons/information.png";
            } else if (severity == Severity.WARNING) {
                fontColor = "grey";
                icon = "/icons/warning.png";
            }
        }
    }


}
