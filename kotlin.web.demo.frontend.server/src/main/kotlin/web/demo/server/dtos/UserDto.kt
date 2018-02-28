package web.demo.server.dtos

import web.demo.server.entity.User

/**
 * POJO for [User]
 *
 * @author Alexander Prendota on 2/26/18 JetBrains.
 */
data class UserDto(var id: Int = 0,
                   var username: String = "",
                   var clientId: String = "",
                   var provider: String = "")
