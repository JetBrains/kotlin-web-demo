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

package org.jetbrains.webdemo.translator;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.plugin.JetLanguage;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.config.EcmaVersion;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WebDemoConfigServer extends Config {

    public WebDemoConfigServer(@NotNull Project project) {
        super(project, REWRITABLE_MODULE_NAME, EcmaVersion.defaultVersion());
    }

    // Hack for HTMLBuilder example (map[] only presented in stdlib)
    @NotNull
    public static final List<String> EXCLUDED_FILES = Arrays.asList(
            "/core/json.kt",
            "/jquery/ui.kt"
    );
    @NotNull
    public static final List<String> ADDITIONAL_FILES = Arrays.asList(

    );

    @NotNull
    public List<JetFile> generateLibFiles() {
        List<JetFile> libFiles = new ArrayList<>();
        ArrayList<String> jsLibFiles = Lists.newArrayList(LIB_FILES_WITH_DECLARATIONS);
        jsLibFiles.addAll(ADDITIONAL_FILES);
        for (String libFileName : jsLibFiles) {
            if (EXCLUDED_FILES.contains(libFileName)) {
                continue;
            }
            JetFile file;
            @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
            File libFile = new File(ApplicationSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "js" + File.separator + libFileName);
            try {
                String text = FileUtil.loadFile(libFile, "UTF-8", true);
                file = (JetFile) PsiFileFactory.getInstance(getProject()).createFileFromText(libFileName, JetLanguage.INSTANCE, text, true, false);
                libFiles.add(file);
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), "unknown", "Cannot load " + libFileName);
            }
        }

        return libFiles;
    }

}
