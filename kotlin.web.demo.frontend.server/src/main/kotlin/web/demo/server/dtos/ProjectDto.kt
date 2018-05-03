package web.demo.server.dtos

import web.demo.server.entity.Project
import web.demo.server.model.ConfType
import web.demo.server.model.ProjectType

/**
 * POJO for [Project]
 *
 * @author Alexander Prendota on 2/7/18 JetBrains.
 */
data class ProjectDto(var id: Int = 0,
                      var name: String = "",
                      var args: String = "",
                      var confType: ConfType = ConfType.java,
                      var publicId: String = "",
                      var type: ProjectType = ProjectType.USER_PROJECT,
                      var expectedOutput: String = "",
                      var compilerVersion: String? = "",
                      var files: List<FileDto> = emptyList(),
                      var readOnlyFileNames: List<String> = emptyList())
