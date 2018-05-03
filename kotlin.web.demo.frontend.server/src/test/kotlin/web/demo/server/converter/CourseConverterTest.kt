package web.demo.server.converter

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.model.course.TaskFile
import web.demo.server.model.stepik.*

/**
 * Test for [CourseConverter]
 * @author Alexander Prendota on 3/15/18 JetBrains.
 */
class CourseConverterTest {

    lateinit var stepikCourse: StepikCourse

    private lateinit var converter: CourseConverter

    @Before
    fun init() {
        converter = CourseConverter()
        val subtaskInfos = StepikSubtaskInfos("JetBrains", "TODO()")
        val placeholder = StepikPlaceholders(listOf(subtaskInfos))
        val test = StepikText("test.kt", "@Test\nfun test(){}")
        val task = StepikTask("Task.kt", "TODO()", listOf(placeholder))
        val text = StepikText("Text", "Please write something")
        val option = StepikOptions("Task", listOf(test), listOf(task), listOf(text))
        val lesson = StepikLesson("42", listOf("1"), "Lesson", listOf(option))

        val addtest = StepikText("unit.kt", "fun helper(){}")
        val addoption = StepikOptions("Task", listOf(addtest), listOf(), listOf())
        val additionalLesson = StepikLesson("42", listOf("1", "2"), GeneralPathsConstants.ADDITIONAL_MATERIALS, listOf(addoption))

        stepikCourse = StepikCourse("4222", "Test Course", listOf(lesson, additionalLesson))
    }

    @Test
    fun testConverter() {
        val course = converter.covertCourseFromStepikFormat(stepikCourse)

        Assert.assertNotNull("Course must not be a null", course)
        Assert.assertEquals(stepikCourse.id, course.id)
        Assert.assertTrue("Lessons must not be empty", course.lessons.isNotEmpty())
        Assert.assertTrue("Should be 1 lesson without ${GeneralPathsConstants.ADDITIONAL_MATERIALS}",
                course.lessons.size == 1)
        Assert.assertEquals(stepikCourse.lessons[0].id, course.lessons[0].id)
        Assert.assertTrue("Tasks must not be empty", course.lessons[0].task.isNotEmpty())
        Assert.assertEquals("Step ID from lesson == task id", stepikCourse.lessons[0].steps[0], course.lessons[0].task[0].id)
        Assert.assertEquals(stepikCourse.lessons[0].task[0].title, course.lessons[0].task[0].title)
        Assert.assertEquals(stepikCourse.lessons[0].task[0].text[0].text, course.lessons[0].task[0].text)
        Assert.assertTrue(course.lessons[0].task[0].courseFiles.map { it.name }.contains("Task.kt"))
        Assert.assertTrue(course.lessons[0].task[0].courseFiles.first { it is TaskFile }.modifiable)
        Assert.assertTrue(course.lessons[0].task[0].courseFiles.map { it.name }.contains("test.kt"))
        Assert.assertTrue(course.lessons[0].task[0].courseFiles.map { it.name }.contains("unit.kt"))
        Assert.assertTrue(course.lessons[0].task[0].readOnlyFileNames.size == 2)
        Assert.assertTrue(course.lessons[0].task[0].courseFiles.size == 3)
    }
}