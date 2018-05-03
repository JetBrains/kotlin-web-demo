package web.demo.server.dtos

/**
 * @author Alexander Prendota on 2/16/18 JetBrains.
 */
data class ExecuteKotlinCodeDto(var project: ProjectDto,
                                var filename: String,
                                var ch: String = "",
                                var line: String = "",
                                var searchForMain: String = ""
)
