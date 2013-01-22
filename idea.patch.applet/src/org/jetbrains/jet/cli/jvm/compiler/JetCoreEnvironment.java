/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.cli.jvm.compiler;

import org.jetbrains.jet.asJava.LightClassGenerationSupport;
import org.jetbrains.jet.internal.com.intellij.codeInsight.ExternalAnnotationsManager;
import org.jetbrains.jet.internal.com.intellij.core.CoreJavaFileManager;
import org.jetbrains.jet.internal.com.intellij.core.JavaCoreApplicationEnvironment;
import org.jetbrains.jet.internal.com.intellij.core.JavaCoreProjectEnvironment;
import org.jetbrains.jet.internal.com.intellij.lang.java.JavaParserDefinition;
import org.jetbrains.jet.internal.com.intellij.mock.MockApplication;
import org.jetbrains.jet.internal.com.intellij.mock.MockProject;
import org.jetbrains.jet.internal.com.intellij.openapi.Disposable;
import org.jetbrains.jet.internal.com.intellij.openapi.components.ServiceManager;
import org.jetbrains.jet.internal.com.intellij.openapi.extensions.Extensions;
import org.jetbrains.jet.internal.com.intellij.openapi.project.Project;
import org.jetbrains.jet.internal.com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.jet.internal.com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.jet.internal.com.intellij.psi.PsiElementFinder;
import org.jetbrains.jet.internal.com.intellij.psi.PsiFile;
import org.jetbrains.jet.internal.com.intellij.psi.PsiManager;
import org.jetbrains.jet.internal.com.intellij.psi.impl.file.impl.JavaFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.asJava.JavaElementFinder;
import org.jetbrains.jet.cli.jvm.JVMConfigurationKeys;
import org.jetbrains.jet.config.CommonConfigurationKeys;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.lang.parsing.JetScriptDefinitionProvider;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.java.JetFilesProvider;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.jet.plugin.JetFileType;
import org.jetbrains.jet.utils.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */

/*
  Need a hack because we cannot add non existing files to classpath
 */
    
public class JetCoreEnvironment {

    private final JavaCoreApplicationEnvironment applicationEnvironment;
    private final JavaCoreProjectEnvironment projectEnvironment;
    private final List<JetFile> sourceFiles = new ArrayList<JetFile>();

    private final CoreExternalAnnotationsManager annotationsManager;

    private final CompilerConfiguration configuration;

    private boolean initialized = false;

    public JetCoreEnvironment(Disposable parentDisposable, @NotNull CompilerConfiguration configuration) {
        this.configuration = configuration.copy();
        this.configuration.setReadOnly(true);

        this.applicationEnvironment = new JavaCoreApplicationEnvironment(parentDisposable);
        applicationEnvironment.registerFileType(JetFileType.INSTANCE, "kt");
        applicationEnvironment.registerFileType(JetFileType.INSTANCE, "kts");
        applicationEnvironment.registerFileType(JetFileType.INSTANCE, "ktm");
        applicationEnvironment.registerFileType(JetFileType.INSTANCE, JetParserDefinition.KTSCRIPT_FILE_SUFFIX); // should be renamed to kts
        applicationEnvironment.registerFileType(JetFileType.INSTANCE, "jet");
        applicationEnvironment.registerParserDefinition(new JavaParserDefinition());
        applicationEnvironment.registerParserDefinition(new JetParserDefinition());

        projectEnvironment = new JavaCoreProjectEnvironment(parentDisposable, applicationEnvironment);

        MockProject project = projectEnvironment.getProject();
        project.registerService(JetScriptDefinitionProvider.class, new JetScriptDefinitionProvider());
        project.registerService(JetFilesProvider.class, new CliJetFilesProvider(this));
        project.registerService(CoreJavaFileManager.class, (CoreJavaFileManager) ServiceManager.getService(project, JavaFileManager.class));
        CliLightClassGenerationSupport cliLightClassGenerationSupport = new CliLightClassGenerationSupport();
        project.registerService(LightClassGenerationSupport.class, cliLightClassGenerationSupport);
        project.registerService(CliLightClassGenerationSupport.class, cliLightClassGenerationSupport);

        Extensions.getArea(project)
                .getExtensionPoint(PsiElementFinder.EP_NAME)
                .registerExtension(new JavaElementFinder(project, cliLightClassGenerationSupport));

        annotationsManager = new CoreExternalAnnotationsManager(project.getComponent(PsiManager.class));
        project.registerService(ExternalAnnotationsManager.class, annotationsManager);
        
        addToClasspath("rt.jar");
        addToClasspath("kotlin-runtime.jar");

        for (File path : configuration.getList(JVMConfigurationKeys.CLASSPATH_KEY)) {
            addToClasspath(path);
        }
        for (File path : configuration.getList(JVMConfigurationKeys.ANNOTATIONS_PATH_KEY)) {
            addExternalAnnotationsRoot(PathUtil.jarFileOrDirectoryToVirtualFile(path));
        }
        for (String path : configuration.getList(CommonConfigurationKeys.SOURCE_ROOTS_KEY)) {
            addSources(path);
        }

        JetScriptDefinitionProvider.getInstance(project).addScriptDefinitions(configuration.getList(CommonConfigurationKeys.SCRIPT_DEFINITIONS_KEY));

        KotlinBuiltIns.initialize(project);
        initialized = true;
    }

    public CompilerConfiguration getConfiguration() {
        return configuration;
    }

    @NotNull
    public MockApplication getApplication() {
        return applicationEnvironment.getApplication();
    }

    @NotNull
    public Project getProject() {
        return projectEnvironment.getProject();
    }

    private void addExternalAnnotationsRoot(VirtualFile root) {
        annotationsManager.addExternalAnnotationsRoot(root);
    }

    private void addSources(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    addSources(child);
                }
            }
        }
        else {
            VirtualFile fileByPath = applicationEnvironment.getLocalFileSystem().findFileByPath(file.getAbsolutePath());
            if (fileByPath != null) {
                PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(fileByPath);
                if (psiFile instanceof JetFile) {
                    sourceFiles.add((JetFile) psiFile);
                }
            }
        }
    }

    private void addSources(String path) {
        if (path == null) {
            return;
        }

        VirtualFile vFile = applicationEnvironment.getLocalFileSystem().findFileByPath(path);
        if (vFile == null) {
            throw new CompileEnvironmentException("File/directory not found: " + path);
        }
        if (!vFile.isDirectory() && vFile.getFileType() != JetFileType.INSTANCE) {
            throw new CompileEnvironmentException("Not a Kotlin file: " + path);
        }

        addSources(new File(path));
    }

    public void addToClasspath(File path) {
        if (initialized) {
            throw new IllegalStateException("Cannot add class path when JetCoreEnvironment is already initialized");
        }
        if (path.isFile()) {
            projectEnvironment.addJarToClassPath(path);
        }
        else {
            final VirtualFile root = applicationEnvironment.getLocalFileSystem().findFileByPath(path.getAbsolutePath());
            if (root == null) {
                throw new IllegalArgumentException("trying to add non-existing file to classpath: " + path);
            }
            projectEnvironment.addSourcesToClasspath(root);
        }
    }

    public void addToClasspath(String path) {
        if (initialized) {
            throw new IllegalStateException("Cannot add class path when JetCoreEnvironment is already initialized");
        }
        final VirtualFile root = VirtualFileManager.getInstance().findFileByUrl("jar://" + path + "!/");
//         = new VirtualJarFile((CoreJarFileSystem) applicationEnvironment.getJarFileSystem(), path);
        if (root == null) {
            throw new IllegalArgumentException("trying to add non-existing file to classpath: " + path);
        }
        
        projectEnvironment.addSourcesToClasspath(root);
    }

    public List<JetFile> getSourceFiles() {
        return sourceFiles;
    }
}
