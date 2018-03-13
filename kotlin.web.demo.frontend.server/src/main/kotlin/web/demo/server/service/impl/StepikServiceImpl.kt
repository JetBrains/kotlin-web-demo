package web.demo.server.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.common.StepikPathsConstants
import web.demo.server.configuration.resourses.EducationCourse
import web.demo.server.dtos.stepik.*
import web.demo.server.http.HttpWrapper
import web.demo.server.service.api.StepikService
import javax.annotation.PostConstruct

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

    @Autowired
    private lateinit var educationCourse: EducationCourse

    private var educationCourses: List<Course> = emptyList()

    @PostConstruct
    fun init() {
        initCourses()
    }

    /**
     * Getting user progress of course from Stepik
     *
     * Add prefix '77-' [PROGRESS_ID_PREFIX] to step id: it's Stepik hac for getting steps by id.
     * Get progress by chunk = 100 [PROGRESS_ID_PREFIX] because of a Stepik constraints for request.
     * After getting the [ProgressContainerDto] remove the '77-' prefix [PROGRESS_ID_PREFIX].
     *
     * @see <a href="https://stepik.org/api/docs/#!/progresses">Stepik API progress</a>
     * @param courseId - string course id from Stepik
     * @param tokenValue - user token after auth
     *
     * @return list of [ProgressDto]
     */
    override fun getCourseProgress(courseId: String, tokenValue: String): List<ProgressDto> {
        val stepsFromCourse = educationCourses.first { it.id == courseId }.lessons.flatMap { it.steps }
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
     * Getting list of loaded courses
     */
    override fun getCourses(): List<Course> {
        return educationCourses
    }

    /**
     * Method for getting all lessons called [Lesson] by course id
     *
     * @see <a href="https://stepik.org/api/docs/#!/lessons">Stepik API lessons</a>
     * @param courseId - string course id from Stepik
     * @param tokenValue - user token after auth
     *
     * @return list of [Lesson]
     */
    private fun getLessonsByCourse(courseId: String, tokenValue: String): List<Lesson> {
        val headers = mapOf("Authorization" to "Bearer " + tokenValue,
                "Content-Type" to "application/json")
        val queryParameters = mapOf(
                "course" to courseId)
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_LESSONS
        val course = httpWrapper.doGet(url, queryParameters, headers, Course::class.java)
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

    /**
     * Init course from stepic.
     * Getting course list of ID from application.yml
     * Loading course information from Stepik
     * @see [EducationCourse]
     */
    private fun initCourses() {
        val coursesIds = educationCourse.courses
        coursesIds.forEach({ createCourse(it) })
    }

    /**
     * Loading course from Stepik and put it to [educationCourses]
     * Set empty string to token value -> no need a token for request
     * @param courseId - pk course from Stepik
     */
    private fun createCourse(courseId: String) {
        val headers = mapOf("Content-Type" to "application/json")
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_STEPS
        val lessons = getLessonsByCourse(courseId, "")
        lessons.forEach {
            val lessonSteps = it.steps
            val stepsDetails = httpWrapper.doGetToStepik(url, lessonSteps, headers, StepsContainer::class.java)
            it.task = stepsDetails.steps.map { it.block }.map { it.options }
        }
        educationCourses = educationCourses.plus(Course(courseId, lessons))
    }

}