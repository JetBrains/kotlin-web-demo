package web.demo.server.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.*
import web.demo.server.common.ActionPathsConstants
import web.demo.server.dtos.ExecuteKotlinCodeDto
import web.demo.server.dtos.ProjectDto
import web.demo.server.exceptions.OperationNotFoundException
import web.demo.server.model.ConfType
import web.demo.server.model.output.ExecutionResult
import web.demo.server.model.output.JUnitExecutionResult
import web.demo.server.model.output.KotlinExecutionResult
import web.demo.server.service.api.KotlinRunnerService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

/**
 * Rest API for regular operation with Kotlin code
 * - run code
 * - getting versions
 * - convert java code to kotlin code
 * - getting complection
 *
 * @return [ResponseEntity]
 * @author Alexander Prendota on 2/6/18 JetBrains.
 *
 */
@RestController
class KotlinRunnerController : BaseController {

    @Autowired
    lateinit var service: KotlinRunnerService

    /**
     * Rest interface for getting kotlin version from config file
     * See application.properties file for getting location of config file with supported kotlin versions
     */
    @GetMapping(ActionPathsConstants.KOTLIN_VERSIONS)
    fun getKotlinVersionController(): ResponseEntity<*> {
        return ResponseEntity(service.getAvailableKotlinVersions(), HttpStatus.OK)
    }

    /**
     * Rest interface for getting kotlin code from java code
     *
     * @param code - string java code.
     *
     * @return string kotlin code
     */
    @GetMapping(ActionPathsConstants.CONVERT_TO_KOTLIN)
    fun convertJavaToKotlinController(@RequestParam("text") code: String): ResponseEntity<*> {
        return ResponseEntity(service.convertToKotlinCode(code), HttpStatus.OK)
    }

    /**
     * Rest interface for getting kotlin code from java code
     *
     * @param executeKotlinCodeDto - [ExecuteKotlinCodeDto]
     *
     * @return string of Map<String, List<ErrorDescriptor>>
     */
    @PostMapping(ActionPathsConstants.HIGHLIGHT)
    fun checkHighlighting(@RequestBody executeKotlinCodeDto: ExecuteKotlinCodeDto): ResponseEntity<*> {
        return ResponseEntity(service.getHighlighting(executeKotlinCodeDto.project), HttpStatus.OK)
    }


    /**
     * Rest interface for running code form [ProjectDto]
     *
     * @param executeKotlinCodeDto       - [ExecuteKotlinCodeDto]
     * searchForMain        - if true -> search main function in file with code
     * fileName             - name of file with code
     *
     * @return [ExecutionResult] output like:
     * [KotlinExecutionResult] - structure:
     * '{"text":"<outStream>Hello, world!\n</outStream>","exception":null,"errors":{"File.kt":[]}}'
     * <outStream> - for correct result
     * <errStream> - for error output
     * OR
     * [JUnitExecutionResult] - structure if the [ProjectDto.confType] == [ConfType.junit]
     */
    @PostMapping(ActionPathsConstants.RUN_KOTLIN)
    fun runKotlinCodeController(@RequestBody executeKotlinCodeDto: ExecuteKotlinCodeDto,
                                session: HttpSession,
                                authentication: OAuth2Authentication?): ResponseEntity<*> {
        val user = getCurrentUserFromSessionOrNull(session)
        val token = if (authentication == null) null else getStepikToken(authentication, session)
        return ResponseEntity(service.runKotlinCode(executeKotlinCodeDto.project,
                executeKotlinCodeDto.filename,
                executeKotlinCodeDto.searchForMain, user, token), HttpStatus.OK)
    }

    /**
     * Get complete Kotlin code by cursor position
     *
     * @param executeKotlinCodeDto   - [ExecuteKotlinCodeDto]
     * project   - [ProjectDto]
     * fileName  - name of file with code
     * line      - line cursor position
     * ch        - character cursor position
     *
     * @return string output like:
     * [{"text":"toInt()","displayText":"toInt()","tail":"Int","icon":"method"}]
     */
    @PostMapping(ActionPathsConstants.COMPLETE_KOTLIN)
    fun completeKotlinCodeController(@RequestBody executeKotlinCodeDto: ExecuteKotlinCodeDto): ResponseEntity<*> {
        return ResponseEntity(service.completeKotlinCode(
                executeKotlinCodeDto.project,
                executeKotlinCodeDto.filename,
                executeKotlinCodeDto.line,
                executeKotlinCodeDto.ch), HttpStatus.OK)
    }

    /**
     * Controller for supporting old web-demo API.
     * Needed for obsolete versions the Kotlin Playground and other client applications that support the old version API.
     *
     * @see <a href="https://github.com/JetBrains/kotlin-playground">Kotlin Playground</a>
     *
     * @param type      - type of operation
     * @param runConf   - running platform [ConfType]
     * @param request   - request with params: filenames, completion info and [ProjectDto]
     *
     * @throws [OperationNotFoundException] - if type of operation is not found
     * @return
     * 1 - type = run               -> [runKotlinCodeController]
     * 2 - type = complete          -> [completeKotlinCodeController]
     * 3 - type = getKotlinVersions -> [getKotlinVersionController]
     * 4 - type = highlight         -> [checkHighlighting]
     */
    @RequestMapping(value = [(ActionPathsConstants.KOTLIN_SERVER)], method = [RequestMethod.POST, RequestMethod.GET])
    fun multipleController(@RequestParam("type") type: String,
                           @RequestParam("runConf", required = false) runConf: String?,
                           session: HttpSession,
                           request: HttpServletRequest): ResponseEntity<*> {
        return when (type) {
            "run" -> runKotlinCodeController(buildObjectsForOldRequests(request), session, authentication = null)
            "complete" -> completeKotlinCodeController(buildObjectsForOldRequests(request))
            "getKotlinVersions" -> getKotlinVersionController()
            "highlight" -> checkHighlighting(buildObjectsForOldRequests(request))
            else -> throw OperationNotFoundException("Can not find operation - $type")
        }
    }

    /**
     * Build [ExecuteKotlinCodeDto] by [HttpServletRequest] object.
     * Getting 'project', 'searchForMain' and 'filename' request parameters for running
     * and also 'ch', 'line' for complete request
     *
     * @param request - request with params: filenames, completion info and [ProjectDto]
     *
     * @return [ExecuteKotlinCodeDto]
     */
    private fun buildObjectsForOldRequests(request: HttpServletRequest): ExecuteKotlinCodeDto {
        val project = jacksonObjectMapper().readValue(request.getParameter("project"), ProjectDto::class.java)
        val fileName = request.getParameter("filename")
        val line = request.getParameter("line") ?: ""
        val ch = request.getParameter("ch") ?: ""
        val searchForMain = request.getParameter("searchForMain") ?: ""
        return ExecuteKotlinCodeDto(project, fileName, ch, line, searchForMain)
    }

}