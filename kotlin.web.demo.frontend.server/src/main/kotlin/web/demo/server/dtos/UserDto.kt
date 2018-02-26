package web.demo.server.dtos

import web.demo.server.entity.User

/**
 * POJO for [User]
 *
 * @author Alexander Prendota on 2/26/18 JetBrains.
 */
data class UserDto(var id: Int,
                   var name: String,
                   var clientId: String)
