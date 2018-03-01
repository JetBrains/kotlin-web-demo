package web.demo.server.dtos

import web.demo.server.entity.Project

/**
 * POJO for [Project]
 *
 * @author Alexander Prendota on 2/7/18 JetBrains.
 */
data class ProjectDto(var id: Int? = 0,
                      var name: String? = "",
                      var args: String? = "",
                      var confType: String? = "",
                      var originUrl: String? = "",
                      var publicId: String? = "",
                      var ownerId: String? = "",
                      var expectedOutput: String? = "",
                      var compilerVersion: String? = "",
                      var files: List<FileDto>? = emptyList(),
                      var readOnlyFileNames: List<String>? = emptyList())
