package web.view.ukhorskaya.errorsDescriptors;

import com.google.common.base.Predicates;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.diagnostics.Diagnostic;
import org.jetbrains.jet.lang.diagnostics.DiagnosticFactory;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.diagnostics.UnresolvedReferenceDiagnostic;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.AnalyzingUtils;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JavaDefaultImports;
import org.json.JSONArray;
import web.view.ukhorskaya.Interval;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 11:24 AM
 */

public class ErrorAnalyzer {
    private final PsiFile currentPsiFile;
    private final Document currentDocument;
    private final Project currentProject;

    private final long startTime = System.nanoTime();

    public ErrorAnalyzer(PsiFile currentPsiFile) {
        this.currentPsiFile = currentPsiFile;
        this.currentProject = currentPsiFile.getProject();
        this.currentDocument = currentPsiFile.getViewProvider().getDocument();
    }

    public List<ErrorDescriptor> getAllErrors() {

        final List<PsiErrorElement> errorElements = new ArrayList<PsiErrorElement>();
        PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PsiErrorElement) {
                    errorElements.add((PsiErrorElement) element);
                }
                super.visitElement(element);
            }
        };

        visitor.visitFile(currentPsiFile);

        final List<ErrorDescriptor> errors = new ArrayList<ErrorDescriptor>();

        for (PsiErrorElement errorElement : errorElements) {
            int start = errorElement.getTextRange().getStartOffset();
            int end = errorElement.getTextRange().getEndOffset();
            Interval interval = new Interval(start, end, currentDocument);
            errors.add(new ErrorDescriptor(interval, errorElement.getErrorDescription(), Severity.ERROR, "red_wavy_line"));
        }
        long startAnalyzeNamespace = System.nanoTime();

        BindingContext bindingContext = AnalyzingUtils.getInstance(JavaDefaultImports.JAVA_DEFAULT_IMPORTS).analyzeNamespaces(
                currentProject,
                Collections.singletonList(((JetFile) currentPsiFile).getRootNamespace()),
                Predicates.<PsiFile>equalTo(currentPsiFile),
                JetControlFlowDataTraceFactory.EMPTY);

        System.out.print("analyze, " + (System.nanoTime() - startAnalyzeNamespace)/1000000 + ", ");
        Collection<Diagnostic> diagnostics = bindingContext.getDiagnostics();

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getSeverity() != Severity.INFO) {
                DiagnosticFactory factory = diagnostic.getFactory();
                int start = factory.getTextRange(diagnostic).getStartOffset();
                int end = factory.getTextRange(diagnostic).getEndOffset();
                String className = diagnostic.getSeverity().name();
                if (!(diagnostic instanceof UnresolvedReferenceDiagnostic) && (diagnostic.getSeverity() == Severity.ERROR)) {
                    className = "red_wavy_line";
                }
                errors.add(new ErrorDescriptor(new Interval(start, end, currentDocument), diagnostic.getMessage(), diagnostic.getSeverity(), className));
            }
        }

        return errors;
    }
}
