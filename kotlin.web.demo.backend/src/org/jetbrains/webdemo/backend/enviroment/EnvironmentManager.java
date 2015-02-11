package org.jetbrains.webdemo.backend.enviroment;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.compiled.ClassFileDecompilers;
import com.intellij.psi.compiled.ClsStubBuilder;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.util.cls.ClsFormatException;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.cli.jvm.compiler.JetCoreEnvironment;

public abstract class EnvironmentManager {

    protected Getter<FileTypeRegistry> registry;
    protected JetCoreEnvironment environment;
    protected Disposable disposable = new Disposable() {
        @Override
        public void dispose() {
        }
    };

    @NotNull
    protected abstract JetCoreEnvironment createEnvironment();

    @NotNull
    public JetCoreEnvironment getEnvironment() {
        if (environment == null) {
            environment = createEnvironment();

            Extensions.getRootArea()
                    .getExtensionPoint(ClassFileDecompilers.EP_NAME)
                    .registerExtension(new WebDemoClassFileDecompilers());

            //throw new IllegalStateException("Environment should be initialized before");
        }
        return environment;
    }

    public Disposable getDisposable() {
        return disposable;
    }

    public Getter<FileTypeRegistry> getRegistry() {
        return registry;
    }

    private static class WebDemoClassFileDecompilers extends ClassFileDecompilers.Full {

        private final ClsStubBuilder stubBuilder = new ClsStubBuilder() {
            @Override
            public int getStubVersion() {
                return 0;
            }

            @Nullable
            @Override
            public PsiFileStub<?> buildFileStub(@NotNull FileContent fileContent) throws ClsFormatException {
                return null;
            }
        };

        @Override
        public boolean accepts(@NotNull VirtualFile virtualFile) {
            return virtualFile.getPath().contains("kotlin-runtime.jar");
        }

        @NotNull
        @Override
        public ClsStubBuilder getStubBuilder() {
            return stubBuilder;
        }

        @NotNull
        @Override
        public FileViewProvider createFileViewProvider(@NotNull VirtualFile virtualFile, @NotNull PsiManager psiManager, boolean physical) {
            return new WebDemoClassFileViewProvider(psiManager, virtualFile, physical);
        }

        private static class WebDemoClassFileViewProvider extends SingleRootFileViewProvider {
            public WebDemoClassFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled) {
                super(manager, virtualFile, eventSystemEnabled);
            }

            @NotNull
            @Override
            public CharSequence getContents() {
                return "";
            }

            @Nullable
            @Override
            protected PsiFile createFile(@NotNull Project project, @NotNull VirtualFile file, @NotNull FileType fileType) {
                return null;
            }

            @NotNull
            @Override
            public SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
                return new WebDemoClassFileViewProvider(getManager(), copy, false);
            }
        }
    }
}
