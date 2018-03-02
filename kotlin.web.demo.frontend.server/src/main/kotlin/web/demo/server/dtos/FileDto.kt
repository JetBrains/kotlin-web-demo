package web.demo.server.dtos

import web.demo.server.entity.File

/**
 * POJO for [File]
 *
 * @author Alexander Prendota on 2/7/18 JetBrains.
 */
data class FileDto(var modifiable: Boolean? = true,
                   var publicId: String? = "",
                   var projectId: String? = "",
                   var text: String? = "",
                   var name: String? = "")