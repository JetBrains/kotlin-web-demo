package web.demo.server.service.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import web.demo.server.common.PathsConstantsBackend
import web.demo.server.dtos.KotlinVersionDto
import web.demo.server.dtos.ProjectDto
import web.demo.server.http.HttpWrapper
import web.demo.server.model.KotlinVersionConfig
import web.demo.server.service.api.KotlinRunnerService
import java.io.File
import java.io.IOException
import javax.annotation.PostConstruct

/**
 * Implementation of [KotlinRunnerService]
 *
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
@Service
class KotlinRunnerServiceImpl : KotlinRunnerService {

    @Value("\${compilers.config.file}")
    lateinit var jsonConfigFolder: String

    @Autowired
    lateinit var modelMapper: ModelMapper

    @Autowired
    lateinit var pathsBackend: PathsConstantsBackend

    @Autowired
    lateinit var http: HttpWrapper

    lateinit var headers: Map<String, String>

    /**
     * Property with available kotlin version. Getting from config file
     * Init on @PostConstruct method
     */
    lateinit var availableKotlinVersionConfigs: List<KotlinVersionConfig>

    /**
     * Init kotlin version from configuration file
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
        availableKotlinVersionConfigs = jacksonObjectMapper().readValue(File(jsonConfigFolder))
    }

    /**
     * Get kotlin version from configuration file in application properties 'compilers.configuration.file'
     *
     * @return list [KotlinVersionDto]
     */
    override fun getAvailableKotlinVersions(): List<KotlinVersionDto> {
        return availableKotlinVersionConfigs.map { convertKotlinVersionToDto(it) }.toList()
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
     * Converter from [KotlinVersionConfig] to [KotlinVersionDto]
     *
     * @param kotlinVersionConfig - [KotlinVersionConfig]
     * @return [KotlinVersionDto]
     */
    private fun convertKotlinVersionToDto(kotlinVersionConfig: KotlinVersionConfig): KotlinVersionDto {
        return modelMapper.map(kotlinVersionConfig, KotlinVersionDto::class.java)
    }

}
