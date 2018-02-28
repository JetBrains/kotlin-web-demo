package web.demo.server.service.api

import web.demo.server.dtos.UserDto

/**
 * @author Alexander Prendota on 2/27/18 JetBrains.
 */
interface UserService {
    fun authorization(userDto: UserDto): UserDto
}