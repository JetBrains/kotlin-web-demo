/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package org.jetbrains.webdemo.loader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import lombok.NoArgsConstructor
import org.jetbrains.webdemo.ApplicationSettings
import org.jetbrains.webdemo.JsonUtils
import org.jetbrains.webdemo.ProjectFile
import org.jetbrains.webdemo.examples.*
import org.jetbrains.webdemo.exception.NoSuchFileException
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Loader structure from koans-course.json
 *
 * @author Alexander Prendota on 12/25/17
 */
@NoArgsConstructor
class KotlinKoansLoader {

    companion object {

        private val COMPILER = "junit"

        /*
        Keys from koans-course.json
        */
        private val SUB_TASK_INFO = "subtask_infos"
        private val PLACEHOLDERS = "placeholders"
        private val NAME = "name"
        private val LESSONS = "lessons"
        private val TITLE = "title"
        private val TASK_TEXTS = "task_texts"
        private val TASK_FILES = "task_files"
        private val TASK_LIST = "task_list"
        private val TEST_FILES = "test_files"
        private val TEXT = "text"
        private val TASK_KT = "Task.kt"
        private val TEST_KT = "Test.kt"
        private val PLACEHOLDER_TEXT = "placeholder_text"
        private val POSSIBLE_ANSWER = "possible_answer"

    }

    /**
     * Loading Kotlin Koans structure from file to [ExamplesFolder]
     */
    fun loadKotlinKoansStructure() {

        val kotlinKoansRootFile = File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + "koans-course.json")
        try {
            BufferedInputStream(FileInputStream(kotlinKoansRootFile)).use { reader ->
                val manifest = JsonUtils.getObjectMapper().readTree(reader) as ObjectNode
                val name = if (manifest.has(TITLE)) manifest.get(TITLE).asText() else "Kotlin tasks"
                val idRoot = "/$name/"
                /*
                 Insert child directory to structure
                */
                val folder = ExamplesFolder(name, idRoot, true, setLevels())
                if (manifest.has(LESSONS)) {
                    for (node in manifest.get(LESSONS)) {
                        val folderName = if (node.has(TITLE)) node.get(TITLE).asText() else ""
                        /*
                         Skip "Edu additional materials" lesson in json structure
                        */
                        if (folderName == "Edu additional materials") {
                            continue
                        }
                        val folderId = idRoot + folderName + "/"
                        val nodeFolder = ExamplesFolder(folderName, folderId, true, null)
                        if (node.has(TASK_LIST)) {
                            for (example in node.get(TASK_LIST)) {
                                nodeFolder.addExample(loadKoansExamples(example, folderId))
                            }
                        }
                        folder.addChildFolder(nodeFolder)
                    }
                }
                ExamplesFolder.ROOT_FOLDER.addChildFolder(folder)
            }
        } catch (e: IOException) {
            System.err.println("Can't load folder: " + e.toString())
        }

    }

    /**
     * Create [Example] object by JsonNode structure from koans-course.json
     *
     * ReadOnlyFilesNames - list of name of files expect Task and Test.
     * HiddenFiles        - files from 'task_utils' key from [JsonNode].
     *
     * @param node - node object with json data
     * @return - [Example] - object with koans data
     */
    private fun loadKoansExamples(node: JsonNode, folderId: String): Example {
        val id = if (node.has(NAME)) replaceSpaces(folderId + node.get(NAME).asText()) else ""
        val textTask = if (node.has(TASK_TEXTS))
            if (node.get(TASK_TEXTS).has("task")) node.get(TASK_TEXTS).get("task").asText() else "" else ""

        val name = if (node.has(NAME)) node.get(NAME).asText() else ""
        val hiddenFiles = loadHiddenFiles(node, id)
        val files = ArrayList<ProjectFile>()
        files.addAll(loadFiles(node, id))
        files.add(loadTestFile(node, id))

        val readOnlyFiles = files.filter { it.name != TASK_KT }.mapTo(ArrayList()) { it.name }
        val hiddenFilesNames = hiddenFiles.mapTo(ArrayList()) { it.name }
        readOnlyFiles.addAll(hiddenFilesNames)

        return Example(id, name, "", COMPILER, id, null, true, files, hiddenFiles, readOnlyFiles, textTask)
    }

    /**
     * Getting utils files from 'task_utils' key in [JsonNode]
     *
     * @param node - [JsonNode]
     * @param path   - first string in url for building publicId
     * @return - list of [ExampleFile] extends [ProjectFile]
     */
    private fun loadHiddenFiles(node: JsonNode, path: String): List<ProjectFile> {
        if (node.has("task_utils")) {
            val utilsFiles = ArrayList<ProjectFile>()
            val entryIterator = node.get("task_utils").fields()
            while (entryIterator.hasNext()) {
                val file = entryIterator.next()
                utilsFiles.add(ExampleFile(file.key, file.value.asText(), path + "/" + file.key, ProjectFile.Type.KOTLIN_FILE, COMPILER, false, true))
            }
            return utilsFiles
        }
        throw NoSuchFileException("No Utils file in kotlin_koans.json structure. File: " + path)
    }

    /**
     * Loading files from node in 'task_files' key
     *
     * @param node - [JsonNode]
     * @param path - first string in url for building publicId
     * @return - list of [ExampleFile]
     */
    private fun loadFiles(node: JsonNode, path: String): List<ExampleFile> {
        if (node.has(TASK_FILES)) {
            val taskIterator = node.get(TASK_FILES).fields()
            val taskFiles = ArrayList<ExampleFile>()
            while (taskIterator.hasNext()) {

                val file = taskIterator.next()
                if (file.value.has("web")) {
                    if (!file.value.get("web").asBoolean()) {
                        continue
                    }
                }
                when (file.key) {
                    TASK_KT -> taskFiles.add(0, loadTaskFile(file, path))
                    else -> taskFiles.add(loadAdditionalFile(file, path))
                }
            }
            return taskFiles
        }

        throw NoSuchFileException("No Task file in kotlin_koans.json structure. File: " + path)
    }

    /**
     * Loading Task.kt file with specific fields
     *
     * @param file - Task.kt file
     * @param path - first string in url for building publicId
     * @return - [TaskFile]
     */
    private fun loadTaskFile(file: Map.Entry<String, JsonNode>, path: String): TaskFile {
        val name = if (file.value.has(NAME)) file.value.get(NAME).asText() else ""
        if (file.value.has(PLACEHOLDERS)) {
            val mapOfAnswers = loadAnswer(file.value.get(PLACEHOLDERS), path)
            var previewTask = if (file.value.has(TEXT)) file.value.get(TEXT).asText() else ""
            var answerTask = if (file.value.has(TEXT)) file.value.get(TEXT).asText() else ""
            for (key in mapOfAnswers.keys) {
                previewTask = previewTask.replace(key, "<taskWindow>$key</taskWindow>")
                (0 until mapOfAnswers[key]!!.size)
                        .forEach { answerTask = answerTask.replaceFirst(key, mapOfAnswers.getValue(key)[it]) }
            }
            return TaskFile(previewTask, answerTask, path + "/" + name)
        }


        throw NoSuchFileException("No Task file in kotlin_koans.json structure. File: " + path)
    }


    /**
     * Getting fields from 'subtask_infos' key from [JsonNode]
     *
     * @param node      - node with key 'subtask_infos'
     * @param path      - String path for exception
     * @return - Map { placeholder -> list of different answers}
     */
    private fun loadAnswer(node: JsonNode, path: String): Map<String, List<String>> {
        if (node.size() > 0) {
            val mapOfAnswers = HashMap<String, List<String>>()
            (0 until node.size())
                    .filter { node.get(it).has(SUB_TASK_INFO) && node.get(it).get(SUB_TASK_INFO).has("0")
                            && node.get(it).get(SUB_TASK_INFO).get("0").has(PLACEHOLDER_TEXT)
                            && node.get(it).get(SUB_TASK_INFO).get("0").has(POSSIBLE_ANSWER) }
                    .forEach {
                        val key = node.get(it).get(SUB_TASK_INFO).get("0").get(PLACEHOLDER_TEXT).asText()
                        val value = node.get(it).get(SUB_TASK_INFO).get("0").get(POSSIBLE_ANSWER).asText()
                        if (mapOfAnswers[key] == null) {
                            mapOfAnswers.put(key, arrayListOf(value))
                        } else {
                            var list = mapOfAnswers[key]!!
                            list = list.plus(value)
                            mapOfAnswers[key] = list
                        }
                    }
            return mapOfAnswers
        }

        throw NoSuchFileException("No answers in kotlin_koans.json structure. File: $path")
    }


    /**
     * Loading files except 'Task.kt' file
     *
     * @param file - map with files {name -> fields}
     * @param path - first string in url for building publicId
     * @return - [ExampleFile]
     */
    private fun loadAdditionalFile(file: Map.Entry<String, JsonNode>, path: String): ExampleFile {
        val name = if (file.value.has(NAME)) file.value.get(NAME).asText() else ""
        val text = if (file.value.has(TEXT)) file.value.get(TEXT).asText() else ""
        return ExampleFile(name, text, path + "/" + name, ProjectFile.Type.KOTLIN_FILE, null, false, false)
    }


    /**
     * Replace all of spaces in string to %20
     *
     * @param url - string
     * @return converted string
     */
    private fun replaceSpaces(url: String): String {
        return url.replace(" ".toRegex(), "%20")
    }


    /**
     * Load test file from 'test_files' from [JsonNode]
     *
     * @param node - [JsonNode]
     * @param path   - string path
     * @return - [TestFile]
     */
    private fun loadTestFile(node: JsonNode, path: String): TestFile {
        if (node.has(TEST_FILES)) {
            if (node.get(TEST_FILES).has(TEST_KT)) {
                return TestFile(node.get(TEST_FILES).get(TEST_KT).asText(), path + "/$TEST_KT")
            }
        }
        throw NoSuchFileException("No Test file in kotlin_koans.json structure. File: " + path)
    }

    /**
     * Getting [LevelInfo]
     *
     * @return - list of [LevelInfo]
     */
    private fun setLevels(): List<LevelInfo> {
        return arrayListOf(
                LevelInfo(8, "#409BCB"),
                LevelInfo(16, "#6A855B"),
                LevelInfo(24, "#FDC474"),
                LevelInfo(32, "#C9783A"),
                LevelInfo(42, "#9976A9")
        )
    }

}
