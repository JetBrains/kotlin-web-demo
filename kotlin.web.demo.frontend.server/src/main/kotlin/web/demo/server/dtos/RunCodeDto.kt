package web.demo.server.dtos

/**
 * @author Alexander Prendota on 2/16/18 JetBrains.
 */
data class RunCodeDto(var project: ProjectDto,
                      var searchForMain: String,
                      var filename: String
)