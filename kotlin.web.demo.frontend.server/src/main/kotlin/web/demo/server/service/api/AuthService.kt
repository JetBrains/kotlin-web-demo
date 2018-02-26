package web.demo.server.service.api

import web.demo.server.dtos.UserDto

/**
 * @author Alexander Prendota on 2/26/18 JetBrains.
 */
interface AuthService {
    fun authorizationUser(detail: Any): UserDto
}