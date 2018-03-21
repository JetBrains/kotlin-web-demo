package web.demo.server.service.api

import web.demo.server.dtos.KotlinVersionDto
import web.demo.server.dtos.ProjectDto
import web.demo.server.dtos.UserDto
import web.demo.server.model.output.ExecutionResult

/**
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
interface KotlinRunnerService {

    fun getAvailableKotlinVersions(): List<KotlinVersionDto>

    fun convertToKotlinCode(code: String): String

    fun runKotlinCode(project: ProjectDto, fileName: String, searchForMain: String, user: UserDto?, token: String?): ExecutionResult

    fun getHighlighting(project: ProjectDto): String

    fun completeKotlinCode(project: ProjectDto, fileName: String, line: String, ch: String): String

}
