/*
 * Copyright 2000-2019 JetBrains s.r.o.
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

@file:Suppress("PRE_RELEASE_CLASS")

package org.jetbrains.webdemo.kotlin.impl.environment

import com.google.common.collect.Lists
import com.intellij.codeInsight.ContainerProvider
import com.intellij.codeInsight.NullabilityAnnotationInfo
import com.intellij.codeInsight.NullableNotNullManager
import com.intellij.codeInsight.runner.JavaMainMethodProvider
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.fileTypes.FileTypeExtensionPoint
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.Getter
import com.intellij.psi.FileContextProvider
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.compiled.ClassFileDecompilers
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy
import com.intellij.psi.meta.MetaDataContributor
import com.intellij.psi.stubs.BinaryFileStubBuilders
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.parseCommandLineArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.utils.PathUtil
import org.jetbrains.webdemo.CommonSettings
import org.jetbrains.webdemo.kotlin.idea.DummyCodeStyleManager
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path

object EnvironmentManager {
    private var registry: Getter<FileTypeRegistry>? = null
    private var environment: KotlinCoreEnvironment? = null
    private val disposable = Disposable { }

    /**
     * This list allows to configure behavior of webdemo compiler. Its effect is equivalent
     * to passing this list of string to CLI compiler.
     *
     * See [org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments] and
     * [org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments] for list of possible flags
     */
    private val additionalCompilerArguments: List<String> = listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
            "-Xuse-experimental=kotlin.time.ExperimentalTime",
            "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
            "-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
            "-Xuse-experimental=kotlin.experimental.ExperimentalTypeInference",
            "-XXLanguage:+InlineClasses"
    )

    val languageVersion: TargetPlatformVersion
        get() = TargetPlatformVersion.NoVersion

    fun init(libraries: List<Path>) {
        environment = createEnvironment(libraries)
    }

    fun reinitializeJavaEnvironment() {
        try {
            val method = environment!!.javaClass.getDeclaredMethod("getApplicationEnvironment")
            method.isAccessible = true
            val applicationEnvironment = method.invoke(environment) as CoreApplicationEnvironment

            ApplicationManager.setApplication(
                    applicationEnvironment.application,
                    registry!!,
                    disposable
            )
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }

    }

    fun getEnvironment(): KotlinCoreEnvironment {
        if (environment == null) {
            throw IllegalStateException("Environment should be initialized before")
        }
        return environment!!
    }

    private fun createEnvironment(libraries: List<Path>): KotlinCoreEnvironment {
        val arguments = K2JVMCompilerArguments()
        parseCommandLineArguments(additionalCompilerArguments, arguments)
        val configuration = CompilerConfiguration()
        configuration.addJvmClasspathRoots(getClasspath(arguments, libraries))
        configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions)
        configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions)
        configuration.put(JVMConfigurationKeys.JDK_HOME, File(CommonSettings.JAVA_HOME))
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "kotlinWebDemo")
        configuration.put(JSConfigurationKeys.TYPED_ARRAYS_ENABLED, true)
        configuration.languageVersionSettings =
                arguments.toLanguageVersionSettings(configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY]!!)
        val environment = KotlinCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        val project = environment.project as MockProject
        project.registerService(NullableNotNullManager::class.java, object : NullableNotNullManager(project) {
            override fun setInstrumentedNotNulls(p0: MutableList<String>) {}
            override fun getInstrumentedNotNulls(): MutableList<String> {
                return mutableListOf()
            }

            override fun isJsr305Default(p0: PsiAnnotation, p1: Array<out PsiAnnotation.TargetType>): NullabilityAnnotationInfo? {
                return null
            }

            override fun setNullables(vararg p0: String?) {}
            override fun getDefaultNotNull(): String {
                return ""
            }

            override fun getNotNulls(): MutableList<String> {
                return mutableListOf()
            }

            override fun getDefaultNullable(): String {
                return ""
            }

            override fun setDefaultNotNull(p0: String) {}
            override fun setNotNulls(vararg p0: String?) {}
            override fun getNullables(): MutableList<String> {
                return mutableListOf()
            }

            override fun setDefaultNullable(p0: String) {}
            override fun isNullable(owner: PsiModifierListOwner, checkBases: Boolean): Boolean {
                return false
            }

            override fun isNotNull(owner: PsiModifierListOwner, checkBases: Boolean): Boolean {
                return true
            }
        })
        project.registerService(CodeStyleManager::class.java, DummyCodeStyleManager())
        registerExtensionPoints(Extensions.getRootArea())
        registry = FileTypeRegistry.ourInstanceGetter
        return environment
    }

    private fun registerExtensionPoints(area: ExtensionsArea) {
        CoreApplicationEnvironment.registerExtensionPoint(area, BinaryFileStubBuilders.EP_NAME, FileTypeExtensionPoint::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, FileContextProvider.EP_NAME, FileContextProvider::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, MetaDataContributor.EP_NAME, MetaDataContributor::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, PsiAugmentProvider.EP_NAME, PsiAugmentProvider::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, JavaMainMethodProvider.EP_NAME, JavaMainMethodProvider::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, ContainerProvider.EP_NAME, ContainerProvider::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, ClsCustomNavigationPolicy.EP_NAME, ClsCustomNavigationPolicy::class.java)
        CoreApplicationEnvironment.registerExtensionPoint<ClassFileDecompilers.Decompiler>(area, ClassFileDecompilers.EP_NAME, ClassFileDecompilers.Decompiler::class.java)
    }

    private fun getClasspath(arguments: K2JVMCompilerArguments, libraries: List<Path>): List<File> {
        val classpath = Lists.newArrayList<File>()
        classpath.addAll(PathUtil.getJdkClassesRoots(File(CommonSettings.JAVA_HOME)))
        libraries.mapTo(classpath) { it.toFile() }
        if (arguments.classpath != null) {
            val elements = arguments.classpath!!.split(delimiters = *charArrayOf(File.pathSeparatorChar), ignoreCase = false, limit = 0)
            elements.mapTo(classpath) { File(it) }
        }
        return classpath
    }
}