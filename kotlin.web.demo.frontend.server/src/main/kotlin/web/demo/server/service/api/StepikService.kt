package web.demo.server.service.api

import web.demo.server.dtos.ProjectDto
import web.demo.server.model.course.Course
import web.demo.server.model.stepik.ProgressDto
import web.demo.server.model.stepik.StepikSolution

/**
 * @author Alexander Prendota on 2/20/18 JetBrains.
 */
interface StepikService {
    fun getCourseProgress(courseId: String, tokenValue: String): List<ProgressDto>
    fun getCourses(): List<Course>
    fun getCoursesTitles(): List<Course>
    fun getCourseById(id: String): Course
    fun getCourseSolutions(tasksIds: List<String>, tokenValue: String): List<StepikSolution>
    fun postSolution(project: ProjectDto, token: String, passed: Boolean)
}