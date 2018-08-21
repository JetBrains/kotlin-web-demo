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

package org.jetbrains.webdemo.kotlin.impl.analyzer

import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor
import org.jetbrains.webdemo.kotlin.datastructures.TextInterval
import org.jetbrains.webdemo.kotlin.datastructures.TextPosition
import org.jetbrains.webdemo.kotlin.exceptions.KotlinCoreException
import org.jetbrains.webdemo.kotlin.impl.ResolveUtils
import org.jetbrains.webdemo.kotlin.impl.WrapperSettings
import java.util.*

class ErrorAnalyzer(private val currentPsiFiles: List<KtFile>, private val currentProject: Project) {

    fun getAllErrors(isJs: Boolean): Map<String, List<ErrorDescriptor>> {
        try {
            val errors = HashMap<String, List<ErrorDescriptor>>()
            for (psiFile in currentPsiFiles) {
                errors.put(psiFile.name, getErrorsByVisitor(psiFile))
            }
            val bindingContext = ResolveUtils.getBindingContext(currentPsiFiles, currentProject, isJs)
            val diagnosticErrors = getErrorsFromDiagnostics(bindingContext.diagnostics.all(), errors)
            errors.putAll(diagnosticErrors)
            return errors
        } catch (e: Throwable) {
            throw KotlinCoreException(e)
        }

    }

    fun getErrorsFromDiagnostics(diagnostics: Collection<Diagnostic>, errors: Map<String, List<ErrorDescriptor>>): Map<String, List<ErrorDescriptor>> {
        val diagnosticErrors: MutableMap<String, List<ErrorDescriptor>> = errors.toMutableMap()
        try {
            for (diagnostic in diagnostics) {
                //fix for errors in js library files
                val virtualFile = diagnostic.psiFile.virtualFile
                if (virtualFile == null || virtualFile.presentableUrl.startsWith(WrapperSettings.JS_LIB_ROOT!!.toString())) {
                    continue
                }
                val render = DefaultErrorMessages.render(diagnostic)
                if (render.contains("This cast can never succeed")) {
                    continue
                }
                if (diagnostic.severity != Severity.INFO) {
                    val textRangeIterator = diagnostic.textRanges.iterator()
                    if (!textRangeIterator.hasNext()) {
                        continue
                    }
                    val firstRange = textRangeIterator.next()

                    var className = diagnostic.severity.name
                    if (!(diagnostic.factory === Errors.UNRESOLVED_REFERENCE) && diagnostic.severity == Severity.ERROR) {
                        className = "red_wavy_line"
                    }
                    val interval = getInterval(firstRange.startOffset, firstRange.endOffset,
                            diagnostic.psiFile.viewProvider.document!!)
                    val currentError = diagnosticErrors[diagnostic.psiFile.name]!!.plus(ErrorDescriptor(interval, render, convertSeverity(diagnostic.severity), className))
                    diagnosticErrors[diagnostic.psiFile.name] = currentError
                }
            }

            for (key in diagnosticErrors.keys) {
                Collections.sort(diagnosticErrors[key], Comparator { o1, o2 ->
                    when {
                        o1.interval.start.line > o2.interval.start.line -> return@Comparator 1
                        o1.interval.start.line < o2.interval.start.line -> return@Comparator -1
                        o1.interval.start.line == o2.interval.start.line -> when {
                            o1.interval.start.ch > o2.interval.start.ch -> return@Comparator 1
                            o1.interval.start.ch < o2.interval.start.ch -> return@Comparator -1
                            o1.interval.start.ch == o2.interval.start.ch -> return@Comparator 0
                            else -> {
                            }
                        }
                    }
                    -1
                })
            }
        } catch (e: Throwable) {
            throw KotlinCoreException(e)
        }
        return diagnosticErrors
    }

    private fun getErrorsByVisitor(psiFile: PsiFile): List<ErrorDescriptor> {
        val errorElements = ArrayList<PsiErrorElement>()
        val visitor = object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                element.acceptChildren(this)
            }

            override fun visitErrorElement(element: PsiErrorElement) {
                errorElements.add(element)
            }

        }

        val errors = ArrayList<ErrorDescriptor>()
        visitor.visitFile(psiFile)
        for (errorElement in errorElements) {
            val start = errorElement.textRange.startOffset
            val end = errorElement.textRange.endOffset
            val interval = getInterval(start, end, psiFile.viewProvider.document!!)
            errors.add(ErrorDescriptor(interval, errorElement.errorDescription,
                    convertSeverity(Severity.ERROR), "red_wavy_line"))
        }
        return errors
    }

    private fun convertSeverity(severity: Severity): org.jetbrains.webdemo.kotlin.datastructures.Severity {
        return when (severity) {
            Severity.ERROR -> org.jetbrains.webdemo.kotlin.datastructures.Severity.ERROR
            Severity.INFO -> org.jetbrains.webdemo.kotlin.datastructures.Severity.INFO
            Severity.WARNING -> org.jetbrains.webdemo.kotlin.datastructures.Severity.WARNING
        }
    }

    private fun getInterval(start: Int, end: Int, currentDocument: Document): TextInterval {
        val lineNumberForElementStart = currentDocument.getLineNumber(start)
        val lineNumberForElementEnd = currentDocument.getLineNumber(end)
        var charNumberForElementStart = start - currentDocument.getLineStartOffset(lineNumberForElementStart)
        var charNumberForElementEnd = end - currentDocument.getLineStartOffset(lineNumberForElementStart)
        if (start == end && lineNumberForElementStart == lineNumberForElementEnd) {
            charNumberForElementStart--
            if (charNumberForElementStart < 0) {
                charNumberForElementStart++
                charNumberForElementEnd++
            }
        }
        val startPosition = TextPosition(lineNumberForElementStart, charNumberForElementStart)
        val endPosition = TextPosition(lineNumberForElementEnd, charNumberForElementEnd)
        return TextInterval(startPosition, endPosition)
    }
}
