package org.jetbrains.demo.ukhorskaya.responseHelpers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.math.RandomUtils;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.demo.ukhorskaya.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.demo.ukhorskaya.exceptions.KotlinCoreException;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.jet.codegen.ClassBuilderFactory;
import org.jetbrains.jet.codegen.ClassFileFactory;
import org.jetbrains.jet.codegen.GenerationState;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.psi.JetFile;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
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

    private final boolean isOnlyCompilation;
    private final PsiFile currentPsiFile;
    private final String arguments;

    private final SessionInfo sessionInfo;

    public CompileAndRunExecutor(boolean onlyCompilation, PsiFile currentPsiFile, String arguments, SessionInfo info) {
        this.isOnlyCompilation = onlyCompilation;
        this.currentPsiFile = currentPsiFile;
        this.arguments = arguments;
        this.sessionInfo = info;
    }

    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile, sessionInfo);
        List<ErrorDescriptor> errors;
        try {
            errors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            return ResponseUtils.getErrorWithStackTraceInJson(ServerSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString());
        }

        if (errors.isEmpty() || isOnlyWarnings(errors)) {
            if (isOnlyCompilation) {
                return "[{\"text\":\"Compilation complete successfully\",\"type\":\"out\"}]";
            }
            Project currentProject = currentPsiFile.getProject();
//            BindingContext bindingContext = AnalyzerFacade.analyzeOneFileWithJavaIntegration(
//                                (JetFile) currentPsiFile, JetControlFlowDataTraceFactory.EMPTY);

            sessionInfo.getTimeManager().saveCurrentTime();
            GenerationState generationState;
            try {
                generationState = new GenerationState(currentProject, ClassBuilderFactory.BINARIES);
                generationState.compile((JetFile) currentPsiFile);
//                generationState.compileCorrectFiles(bindingContext, Collections.singletonList((JetFile) currentPsiFile));
            } catch (Throwable e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
                return ResponseUtils.getErrorWithStackTraceInJson(ServerSettings.KOTLIN_ERROR_MESSAGE, new KotlinCoreException(e).getStackTraceString());
            }
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "COMPILE correctNamespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            StringBuilder stringBuilder = new StringBuilder("Generated classfiles: ");
            stringBuilder.append(ResponseUtils.addNewLine());
            final ClassFileFactory factory = generationState.getFactory();

            sessionInfo.getTimeManager().saveCurrentTime();
            List<String> files = factory.files();
            File outputDir = new File(ServerSettings.OUTPUT_DIRECTORY + File.separator + "tmp" + RandomUtils.nextInt());
            boolean isOutputExists = true;
            if (!outputDir.exists()) {
                isOutputExists = outputDir.mkdirs();
            }
            for (String file : files) {
//                File outputDir = new File(ServerSettings.OUTPUT_DIRECTORY);
//                ServerSettings.OUTPUT_DIRECTORY = outputDir.getAbsolutePath();
                if (isOutputExists) {
                    File target = new File(outputDir, file);
                    try {
                        FileUtil.writeToFile(target, factory.asBytes(file));
                        stringBuilder.append(file).append(ResponseUtils.addNewLine());
                    } catch (IOException e) {
                        ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e,
                                currentPsiFile.getText()));
                        return ResponseUtils.getErrorInJson("Cannot get a completion.");
                    }
                } else {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(),
                            "Cannot create output directory for files: " + outputDir.getAbsolutePath(), currentPsiFile.getText()));
                    return ResponseUtils.getErrorInJson("Error on server: cannot run your program.");
                }

            }
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "Write files on disk " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            JSONArray jsonArray = new JSONArray();
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", "info");
            map.put("text", stringBuilder.toString());
            jsonArray.put(map);

            JavaRunner runner = new JavaRunner(files, arguments, jsonArray, currentPsiFile.getText(), sessionInfo);

            return runner.getResult(outputDir.getAbsolutePath());

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
