package web.demo.server.converter

import org.springframework.stereotype.Component
import web.demo.server.dtos.course.*
import web.demo.server.dtos.stepik.*
import web.demo.server.model.ConfType


/**
 * @author Alexander Prendota on 3/14/18 JetBrains.
 */

@Component
class CourseConverter {

    val ADDITIONAL_MATERIALS = "PyCharm additional materials"
    lateinit var additionalFiles: List<CourseFile>

    /**
     * Convert raw [StepikCourse] object to [Course] object.
     * Use [ADDITIONAL_MATERIALS] title in [StepikLesson] for filling [additionalFiles]
     *
     * NOTE: Converter does not process the lesson with title [ADDITIONAL_MATERIALS]
     * @param stepikCourse - [StepikCourse]
     * @return [Course]
     */
    fun covertCourseFromStepikFormat(stepikCourse: StepikCourse): Course {
        val lessons = stepikCourse.lessons
        createAdditionalFiles(lessons)
        val chapters = lessons
                .filter { it.title != ADDITIONAL_MATERIALS }
                .map { createChapter(it) }
        return Course(stepikCourse.id, stepikCourse.title, chapters)
    }


    /**
     * Crete [Chapter] from [StepikLesson]
     *
     * @return [Chapter]
     */
    private fun createChapter(stepikLesson: StepikLesson): Chapter {
        val lessons = createLesson(stepikLesson.task, stepikLesson.steps)
        return Chapter(stepikLesson.id, stepikLesson.title, lessons)
    }

    /**
     * Create [Lesson] with [TaskFile] and [CourseFile]
     *
     * NOTE:
     *  - [Lesson.id]   - [StepikSteps.id]. Get it from [stepsIds] index because [stepsIds] size = [stepikOptions] size
     *  - [Lesson.text] - only first text in [StepikOptions]. Others texts elements need for SubTask feature from Stepik and EDU Plugin
     *  - [Lesson.args] - currently empty string
     *  - [Lesson.expectedOutput] - currently false
     *  - [Lesson.compilerVersion] - currently empty string
     *
     * @param stepikOptions - list of [StepikOptions] for converting to list of [Lesson]
     * @param stepsIds      - list of [Lesson.id]
     *
     * @see <a href="https://www.jetbrains.com/pycharm-edu/educators/#subtask-management">SubTask Information</a>
     *
     * @return list of [Lesson]
     */
    private fun createLesson(stepikOptions: List<StepikOptions>, stepsIds: List<String>): List<Lesson> {
        var lessons = emptyList<Lesson>()
        val emptyArgs = ""
        val confType = ConfType.junit.name
        for ((stepsCounter, option) in stepikOptions.withIndex()) {
            val title = option.title
            val id = stepsIds[stepsCounter]
            val textLesson = if (option.text.isNotEmpty()) option.text[0].text else ""
            val readOnlyFilesNames = getReadOnlyFilesName(option.files, option.test)
            val courseFiles = listOf(additionalFiles,
                    createTestFiles(option.test, hidden = false),
                    createTaskFiles(option.files, hidden = false)).flatten()
            val lesson = Lesson(id, title, textLesson, confType, emptyArgs, "", false, readOnlyFilesNames, courseFiles)
            lessons = lessons.plus(lesson)
        }
        return lessons
    }

    /**
     * Create [TaskFile] from [StepikTask] object
     *
     * NOTE: property [TaskFile.modifyAble] depends on list of [PlaceholderAnswer] size
     * @param stepikTasks - list of task files for creating [StepikTask] objects
     * @param hidden      - [CourseFile.hidden] property
     *
     * @return list of [TaskFile]
     */
    private fun createTaskFiles(stepikTasks: List<StepikTask>, hidden: Boolean): List<TaskFile> {
        return stepikTasks.map {
            val answers = it.placeholders
                    .map { it.subtask_infos }
                    .flatten()
                    .map { PlaceholderAnswer(it.placeholder_text, it.possible_answer) }
            TaskFile(it.name, answers.isNotEmpty(), it.text, answers, hidden)
        }
    }

    /**
     * Create test files from [StepikText] object
     * NOTE: All test files have [CourseFile.modifyAble] = false property
     *
     * @param stepikTests - list of test files for creating [CourseFile] objects
     * @param hidden      - [CourseFile.hidden] property
     *
     * @return list of [CourseFile]
     */
    private fun createTestFiles(stepikTests: List<StepikText>, hidden: Boolean): List<CourseFile> {
        val modifyAble = false
        return stepikTests.map { CourseFile(it.name, modifyAble, it.text, hidden) }
    }

    /**
     * Getting names of Read Only files in lesson.
     *
     * What read only file mean?
     * Read only files - files with empty [PlaceholderAnswer] objects. It's files user can not edit because it'a not a task
     *
     * NOTE: Add to result the hidden [additionalFiles] because they are read only too.
     *
     * @param files - lists of [StepikTask] or [StepikText]
     * @return list string names of files. [additionalFiles] included
     */
    private fun getReadOnlyFilesName(vararg files: List<StepikText>): List<String> {
        var readOnlyFile = emptyList<String>()
        files.flatMap { it.toList() }.forEach {
            when (it) {
                is StepikTask -> if (it.placeholders.isEmpty()) readOnlyFile = readOnlyFile.plus(it.name)
                else -> readOnlyFile = readOnlyFile.plus(it.name)
            }
        }
        return readOnlyFile.plus(additionalFiles.map { it.name })
    }

    /**
     * Getting additions course files
     *
     * NOTE: All additional files from [Course] are located in one of the course lessons with title [ADDITIONAL_MATERIALS]
     * Add `test` and `files` with hidden modifier to [additionalFiles]
     *
     * @param lessons - list of course lessons
     */
    private fun createAdditionalFiles(lessons: List<StepikLesson>) {
        val lesson = lessons.find { it.title == ADDITIONAL_MATERIALS }
        if (lesson != null) {
            additionalFiles = emptyList()
            val files = lesson.task.map { it.files }.map { createTaskFiles(it, hidden = true) }.flatten()
            val tests = lesson.task.map { it.test }.map { createTestFiles(it, hidden = true) }.flatten()
            additionalFiles = additionalFiles.plus(files)
            additionalFiles = additionalFiles.plus(tests)
        }
    }

}