package web.demo.server.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.common.StepikPathsConstants
import web.demo.server.configuration.resourses.EducationCourse
import web.demo.server.converter.CourseConverter
import web.demo.server.dtos.ProjectDto
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.http.HttpWrapper
import web.demo.server.model.course.Course
import web.demo.server.model.course.CourseFile
import web.demo.server.model.course.Lesson
import web.demo.server.model.stepik.*
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

    @Autowired
    private lateinit var courseConverter: CourseConverter

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
     * @param courseId   - string course id from Stepik
     * @param tokenValue - user token after auth
     *
     * @return list of [ProgressDto]
     */
    override fun getCourseProgress(courseId: String, tokenValue: String): List<ProgressDto> {
        val stepsFromCourse = educationCourses.first { it.id == courseId }.lessons
                .flatMap { it.task }
                .map { it.id }
        val stepsIdToRequest = prepareTaskIdToRequest(stepsFromCourse)
        val headers = prepareHeaders(tokenValue)
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
     * Getting [StepikSolution] of the [CourseFile]
     * Use [StepikSubmissionContainer] structure for getting [StepikSolution]
     * Get first element in list of solutions in response with name 'Task.kt'
     *
     * @param tasksIds   - id from [CourseFile]
     * @param tokenValue - user token after auth
     *
     * @return list of [StepikSolution]
     */
    override fun getCourseSolutions(tasksIds: List<String>, tokenValue: String): List<StepikSolution> {
        val headers = prepareHeaders(tokenValue)
        val queryParameters = mutableMapOf(
                "status" to "correct",
                "page" to "1",
                "order" to "desc")
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_SUBMISSIONS
        var solutions = emptyList<StepikSolution>()
        tasksIds.forEach {
            queryParameters["step"] = it
            val solution = httpWrapper.doGet(url, queryParameters, headers, StepikSubmissionContainer::class.java)
                    .submissions
                    .map { it.reply }
                    .map { it.solution }
                    .flatten()
                    .firstOrNull { it.name == "Task.kt" }
            if (solution != null) solutions = solutions.plus(solution)
        }
        return solutions
    }

    /**
     * Getting list of loaded courses
     */
    override fun getCourses(): List<Course> {
        return educationCourses
    }

    /**
     * Getting [Course.id] and [Course.title] from [educationCourses]
     * @return list [Course] with only id and title
     */
    override fun getCoursesTitles(): List<Course> {
        return educationCourses.map { Course(it.id, it.title, emptyList()) }
    }

    /**
     * Getting full course by id
     *
     * @throws [SourceNotFoundException] - if course not found
     * @return [Course]
     */
    override fun getCourseById(id: String): Course {
        return educationCourses.firstOrNull { it.id == id }
                ?: throw SourceNotFoundException("Could not find course with id: $id")
    }

    /**
     * Post solution into Stepik
     * Get first task file with name [GeneralPathsConstants.TASK_NAME] and sent file content to Stepik
     * NOTE:
     * Stepik API structure need only list of solutions
     */
    override fun postSolution(project: ProjectDto, token: String, passed: Boolean) {
        val attempt = postAttempt(project.id.toString(), token)
        val taskFile = project.files.first { it.name == GeneralPathsConstants.TASK_NAME }
        val solutions = listOf(StepikSolution(taskFile.name, taskFile.text))
        postSubmission(passed, attempt, solutions, token)
    }

    /**
     * Post submission to Stepik
     * Use [SubmissionWrapper] structure for send request to Stepik
     * Set [StepikReply.score] 1 - passed task 0 - no passed
     */
    private fun postSubmission(passed: Boolean, attempt: StepikAttempt,
                               solution: List<StepikSolution>, token: String) {
        val headers = prepareHeaders(token)
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_SUBMISSIONS
        val score = if (passed) "1" else "0"
        val stepikSubmission = SubmissionWrapper(StepikSubmission(StepikReply(solution, score), attempt.id))
        httpWrapper.doPost(url, emptyMap(), headers, stepikSubmission, String::class.java)
    }

    /**
     * Method for getting all lessons called [StepikLesson] by course id
     *
     * @see <a href="https://stepik.org/api/docs/#!/lessons">Stepik API lessons</a>
     * @param courseId - string course id from Stepik
     *
     * @return list of [StepikLesson]
     */
    private fun getLessonsByCourse(courseId: String): List<StepikLesson> {
        val headers = mapOf("Content-Type" to "application/json")
        val queryParameters = mapOf(
                "course" to courseId)
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_LESSONS
        val course = httpWrapper.doGet(url, queryParameters, headers, StepikCourse::class.java)
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
     *
     * Setting [StepikCourse] from [StepikStepsContainer] because of uselessness [StepikBlock], [StepikSteps] objects
     * List of [StepikSteps] id are located in [StepikLesson] object and will be convert to [Lesson] id in [CourseConverter]
     * That's why there's no need to convert them to [StepikOptions] object
     * Use [CourseConverter] for mapping [StepikCourse] to [Course]
     * @param courseId - pk course from Stepik
     */
    private fun createCourse(courseId: String) {
        val headers = mapOf("Content-Type" to "application/json")
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_STEPS
        val lessons = getLessonsByCourse(courseId)
        lessons.forEach {
            val lessonSteps = it.steps
            val stepsDetails = httpWrapper.doGetToStepik(url, lessonSteps, headers, StepikStepsContainer::class.java)
            it.task = stepsDetails.steps.map { it.block }.map { it.options }
        }
        val stepikCourse = getCourseInfo(courseId)
        stepikCourse.lessons = lessons
        val course = courseConverter.covertCourseFromStepikFormat(stepikCourse)
        educationCourses = educationCourses.plus(course)
    }

    /**
     * Getting detail about course from Stepik
     * Stepik return list of courses by request.
     * Because of sending single request -> get first course from list.
     *
     * @param courseId - pk of the course. Sending by @PathParams
     *
     * @return [StepikCourse]
     */
    private fun getCourseInfo(courseId: String): StepikCourse {
        val headers = mapOf("Content-Type" to "application/json")
        val url = "${StepikPathsConstants.STEPIK_API_URL}${StepikPathsConstants.STEPIK_COURSE}$courseId"
        return httpWrapper.doGet(url, emptyMap(), headers, StepikCourseContainer::class.java).courses.first()
    }

    /**
     * Create attempt by step [Lesson.id] id
     *
     * NOTE:
     * Attempt resource. To pass a quiz, user needs to create an attempt and then a submission for the attempt.
     * Attempts are only allowed on quiz steps.
     *
     * Why get first element?
     * - Current attempt with status `active` always first element in list of attempts
     *
     * Why [StepikAttempt.id] in [AttemptWrapper] is empty string?
     * - No need to send attempt id. It's request for creating new attempt for solution
     *
     * @param stepId - [Lesson.id] for creating attempt
     * @param token  - user token after auth
     *
     * @return [StepikAttempt]
     */
    private fun postAttempt(stepId: String, token: String): StepikAttempt {
        val headers = prepareHeaders(token)
        val url = StepikPathsConstants.STEPIK_API_URL + StepikPathsConstants.STEPIK_ATTEMPTS
        val body = AttemptWrapper(StepikAttempt("", stepId))
        val attempts = httpWrapper.doPost(url, emptyMap(), headers, body, StepikAttemptContainer::class.java).attempts
        if (attempts.isEmpty()) throw SourceNotFoundException("Can not get attempt for stepId: $stepId")
        return attempts[0]
    }

    /**
     * Setting Content-Type - application/json header
     * and
     * Authorization -  Bearer + token
     * @param token - string token for Authorization header
     * @return map of headers
     */
    private fun prepareHeaders(token: String): Map<String, String> {
        return mapOf("Authorization" to "Bearer " + token,
                "Content-Type" to "application/json")
    }

}