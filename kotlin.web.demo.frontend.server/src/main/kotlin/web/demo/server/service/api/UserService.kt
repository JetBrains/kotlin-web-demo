package web.demo.server.service.api

import web.demo.server.dtos.UserDto
import web.demo.server.entity.User

/**
 * @author Alexander Prendota on 2/27/18 JetBrains.
 */
interface UserService {
    fun authorization(userDto: UserDto): UserDto
    fun defineUser(clientId: String): User
}