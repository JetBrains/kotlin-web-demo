package web.demo.server.dtos

/**
 * @author Alexander Prendota on 2/7/18 JetBrains.
 */
data class ProjectDto(var id: String?,
                      var name: String?,
                      var args: String?,
                      var confType: String?,
                      var originUrl: String?,
                      var expectedOutput: String?,
                      var compilerVersion: String?,
                      var files: List<FileDto>?,
                      var readOnlyFileNames: List<String>?)
