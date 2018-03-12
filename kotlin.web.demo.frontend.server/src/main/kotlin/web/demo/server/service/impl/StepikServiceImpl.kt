package web.demo.server.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.common.StepikPathsConstants
import web.demo.server.dtos.stepik.CourseDto
import web.demo.server.dtos.stepik.LessonDto
import web.demo.server.dtos.stepik.ProgressContainerDto
import web.demo.server.dtos.stepik.ProgressDto
import web.demo.server.http.HttpWrapper
import web.demo.server.service.api.StepikService

/**
 * Implementation of [StepikService]
 *
 * @author Alexander Prendota on 2/20/18 JetBrains.
 */
@Service
class StepikServiceImpl : StepikService {

    /**
     * Prefix for getting task ids from Stepik for easy way
     */
    private val PROGRESS_ID_PREFIX = "77-"
    /**
     * Restriction of Stepik API for multiple requests.
     * 100 parameters it's a Stepik constraints for request.
     */
    private val MAX_REQUEST_PARAMS = 100

    @Autowired
    private lateinit var httpWrapper: HttpWrapper

    /**
     * Getting user progress of course from Stepik
     *
     * Add prefix '77-' [PROGRESS_ID_PREFIX] to step id: it's Stepik hac for getting steps by id.
     * Get progress by chunk = 100 [PROGRESS_ID_PREFIX] because it's a Stepik constraints for request.
     * After getting the [ProgressContainerDto] remove the '77-' prefix [PROGRESS_ID_PREFIX].
     *
     * @see <a href="https://stepik.org/api/docs/#!/progresses">Stepik API progress</a>
     * @param courseId - string course id from Stepik
     * @param tokenValue - user token after auth
     *
     * @return list of [ProgressDto]
     */
    override fun getCourseProgress(courseId: String, tokenValue: String): List<ProgressDto> {
        val stepsFromCourse = getStepsFromCourse(courseId, tokenValue)
        val stepsIdToRequest = prepareTaskIdToRequest(stepsFromCourse)
        val headers = mapOf("Authorization" to "Bearer " + tokenValue,
                "Content-Type" to "application/json")
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_PROGRESSES
        var progressContainer = emptyList<ProgressContainerDto>()
        var iterator = 0
        while (iterator < stepsIdToRequest.size) {
            val sublist = stepsIdToRequest.subList(iterator, Math.min(iterator + MAX_REQUEST_PARAMS, stepsIdToRequest.size))
            progressContainer = progressContainer.plus(httpWrapper.doGetToStepik(url, sublist, headers, ProgressContainerDto::class.java))
            iterator += MAX_REQUEST_PARAMS
        }
        return progressContainer.flatMap { it.progresses }.map { ProgressDto(it.id.removePrefix("77-"), it.is_passed) }.toList()
    }

    /**
     * Method for getting step ids from Stepik
     *
     * Use [getLessonsByCourse] for getting [LessonDto] with step ids
     *
     * @param courseId - string course id from Stepik
     * @param tokenValue - user token after auth
     *
     * @return list of string steps id
     */
    private fun getStepsFromCourse(courseId: String, tokenValue: String): List<String> {
        val lessons = getLessonsByCourse(courseId, tokenValue)
        return lessons.flatMap { it.steps }.toList()
    }

    /**
     * Method for getting all lessons called [LessonDto] by course id
     *
     * @see <a href="https://stepik.org/api/docs/#!/lessons">Stepik API lessons</a>
     * @param courseId - string course id from Stepik
     * @param tokenValue - user token after auth
     *
     * @return list of [LessonDto]
     */
    private fun getLessonsByCourse(courseId: String, tokenValue: String): List<LessonDto> {
        val headers = mapOf("Authorization" to "Bearer " + tokenValue,
                "Content-Type" to "application/json")
        val queryParameters = mapOf(
                "course" to courseId)
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_LESSONS
        val course = httpWrapper.doGet(url, queryParameters, headers, CourseDto::class.java)
        return course.lessons
    }

    /**
     * Adding [PROGRESS_ID_PREFIX] to task ID.
     * For getting task entity from Stepik for easy way.
     *
     * @param taskIds - string task id
     *
     * @return taskId with prefix [PROGRESS_ID_PREFIX]
     */
    private fun prepareTaskIdToRequest(taskIds: List<String>): List<String> {
        return taskIds.map { "$PROGRESS_ID_PREFIX$it" }
    }
}