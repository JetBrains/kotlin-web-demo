/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.impl.environment

import org.jetbrains.webdemo.CommonSettings
import org.jetbrains.webdemo.kotlin.idea.DummyCodeStyleManager
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path

object EnvironmentManager {
    private var registry: Getter<FileTypeRegistry>? = null
    private var environment: KotlinCoreEnvironment? = null
    private val disposable = Disposable { }

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
        val configuration = CompilerConfiguration()

        configuration.addJvmClasspathRoots(getClasspath(arguments, libraries))

        configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions)
        configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions)
        configuration.put(JVMConfigurationKeys.JDK_HOME, File(CommonSettings.JAVA_HOME))

        configuration.put(CommonConfigurationKeys.MODULE_NAME, "kotlinWebDemo")

        configuration.put(JSConfigurationKeys.TYPED_ARRAYS_ENABLED, true)

        val environment = KotlinCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        val project = environment.project as MockProject
        project.registerService(NullableNotNullManager::class.java, object : NullableNotNullManager(project) {
            override fun isNullable(owner: PsiModifierListOwner, checkBases: Boolean): Boolean {
                return false
            }

            override fun isNotNull(owner: PsiModifierListOwner, checkBases: Boolean): Boolean {
                return true
            }

            override fun getPredefinedNotNulls(): List<String> {
                return emptyList()
            }

            override fun hasHardcodedContracts(element: PsiElement?): Boolean {
                return false
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
