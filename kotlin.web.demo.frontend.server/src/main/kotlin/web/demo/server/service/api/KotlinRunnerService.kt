package web.demo.server.service.api

import web.demo.server.dtos.KotlinVersionDto
import web.demo.server.dtos.ProjectDto
import web.demo.server.model.output.ExecutionResult

/**
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
interface KotlinRunnerService {

    fun getAvailableKotlinVersions(): List<KotlinVersionDto>

    fun convertToKotlinCode(code: String): String

    fun runKotlinCode(project: ProjectDto, fileName: String, searchForMain: String): ExecutionResult

    fun completeKotlinCode(project: ProjectDto, fileName: String, line: String, ch: String): String

}
