package web.demo.server.service.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.common.BackendPathsConstants
import web.demo.server.configuration.KotlinVersion
import web.demo.server.dtos.KotlinVersionDto
import web.demo.server.dtos.ProjectDto
import web.demo.server.exceptions.ValidationException
import web.demo.server.http.HttpWrapper
import web.demo.server.service.api.KotlinRunnerService
import java.io.IOException
import javax.annotation.PostConstruct

/**
 * Implementation of [KotlinRunnerService]
 *
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
@Service
class KotlinRunnerServiceImpl : KotlinRunnerService {

    @Autowired
    lateinit var kotlinVersions: KotlinVersion

    @Autowired
    private lateinit var pathsBackend: BackendPathsConstants

    @Autowired
    private lateinit var http: HttpWrapper

    private lateinit var headers: Map<String, String>

    /**
     * Property with available kotlin version. Getting from application.yml
     * Init on @PostConstruct method
     */
    private var availableKotlinVersionConfigs: List<KotlinVersionDto> = emptyList()

    /**
     * Init kotlin version from application.yml
     * Init headers for requests
     * @throws IOException - exception
     */
    @PostConstruct
    @Throws(IOException::class)
    fun init() {
        headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "*/*"
        )
        initKotlinVersions()
    }

    /**
     * Get kotlin version from configuration file in application properties
     *
     * @return list [KotlinVersionDto]
     */
    override fun getAvailableKotlinVersions(): List<KotlinVersionDto> {
        return availableKotlinVersionConfigs
    }


    /**
     * Convert Java to Kotlin
     * Send http get to server
     * @param code - string java code for converting
     *
     * @return string Kotlin code
     */
    override fun convertToKotlinCode(code: String): String {
        val queryParameters = mapOf(
                "type" to pathsBackend.CONVERT_TO_KOTLIN,
                "text" to code)
        return http.doGet(pathsBackend.SERVER_PATH, queryParameters, headers, String::class.java)
    }


    /**
     * Run kotlin code
     *
     * @param project       - [ProjectDto]
     * @param fileName      - name of file
     * @param searchForMain - if true -> search main function in file with code
     *
     * @return string json response
     */
    override fun runKotlinCode(project: ProjectDto, fileName: String, searchForMain: String): String {
        val typeOfCode = project.confType ?: "java"
        val projectAsString = jacksonObjectMapper().writeValueAsString(project)
        val queryParameters = mapOf(
                "type" to pathsBackend.RUN_KOTLIN,
                "runConf" to typeOfCode,
                "project" to projectAsString,
                "filename" to fileName,
                "searchForMain" to searchForMain)
        return http.doGet(pathsBackend.SERVER_PATH, queryParameters, headers, String::class.java)
    }


    /**
     * Get complete Kotlin code by cursor position
     *
     * @param project   - [ProjectDto]
     * @param fileName  - name of file with code
     * @param line      - line cursor position
     * @param ch        - character cursor position
     *
     * @return string json response
     */
    override fun completeKotlinCode(project: ProjectDto, fileName: String, line: String, ch: String): String {
        val typeOfCode = project.confType ?: "java"
        val projectAsString = jacksonObjectMapper().writeValueAsString(project)
        val queryParameters = mapOf(
                "type" to pathsBackend.COMPLETE_KOTLIN,
                "runConf" to typeOfCode,
                "project" to projectAsString,
                "filename" to fileName,
                "ch" to ch,
                "line" to line)
        return http.doGet(pathsBackend.SERVER_PATH, queryParameters, headers, String::class.java)
    }

    /**
     * Getting kotlin versions.
     * Map to [KotlinVersionDto]
     *
     * @throws [ValidationException] if no the latest stable kotlin version
     */
    private fun initKotlinVersions() {
        val latestVersion = kotlinVersions.latestStable
                ?: throw ValidationException("No the latest stable kotlin version in application.yml file")
        kotlinVersions.versions.map {
            availableKotlinVersionConfigs = availableKotlinVersionConfigs.plus(KotlinVersionDto(it, latestVersion == it))
        }
    }

}
