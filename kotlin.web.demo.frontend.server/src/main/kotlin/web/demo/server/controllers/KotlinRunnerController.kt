package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import web.demo.server.common.PathsConstants
import web.demo.server.dtos.ExecuteKotlinCodeDto
import web.demo.server.dtos.ProjectDto
import web.demo.server.service.api.KotlinRunnerService

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
class KotlinRunnerController {

    @Autowired
    lateinit var service: KotlinRunnerService

    /**
     * Rest interface for getting kotlin version from config file
     * See application.properties file for getting location of config file with supported kotlin versions
     */
    @GetMapping(PathsConstants.KOTLIN_VERSIONS)
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
    @GetMapping(PathsConstants.CONVERT_TO_KOTLIN)
    fun convertJavaToKotlinController(@RequestParam("text") code: String): ResponseEntity<*> {
        return ResponseEntity(service.convertToKotlinCode(code), HttpStatus.OK)
    }


    /**
     * Rest interface for running code form [ProjectDto]
     *
     * @param executeKotlinCodeDto       - [ExecuteKotlinCodeDto]
     * searchForMain        - if true -> search main function in file with code
     * fileName             - name of file with code
     *
     * @return string output like:
     *
     * '{"text":"<outStream>Hello, world!\n</outStream>","exception":null,"errors":{"File.kt":[]}}'
     * <outStream> - for correct result
     * <errStream> - for error output
     */
    @PostMapping(PathsConstants.RUN_KOTLIN)
    fun runKotlinCodeController(@RequestBody executeKotlinCodeDto: ExecuteKotlinCodeDto): ResponseEntity<*> {
        return ResponseEntity(service.runKotlinCode(executeKotlinCodeDto.project,
                executeKotlinCodeDto.filename,
                executeKotlinCodeDto.searchForMain), HttpStatus.OK)
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
    @PostMapping(PathsConstants.COMPLETE_KOTLIN)
    fun completeKotlinCodeController(@RequestBody executeKotlinCodeDto: ExecuteKotlinCodeDto): ResponseEntity<*> {
        return ResponseEntity(service.completeKotlinCode(
                executeKotlinCodeDto.project,
                executeKotlinCodeDto.filename,
                executeKotlinCodeDto.line,
                executeKotlinCodeDto.ch), HttpStatus.OK)
    }

}