package web.demo.server.dtos

/**
 * @author Alexander Prendota on 2/7/18 JetBrains.
 */
data class FileDto(var modifiable: Boolean?,
                   var publicId: String?,
                   var text: String?,
                   var name: String?,
                   var type: String?)