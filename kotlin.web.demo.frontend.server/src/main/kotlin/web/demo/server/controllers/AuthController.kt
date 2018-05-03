package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import web.demo.server.dtos.UserDto
import web.demo.server.service.api.UserService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession


/**
 * @author Alexander Prendota on 2/19/18 JetBrains.
 */
@Controller
class AuthController: BaseController {

    @Value("\${client.home}")
    lateinit var clientHomeUrl: String

    @Autowired
    lateinit var userService: UserService

    /**
     * Authorization user in web-demo.
     */
    @GetMapping("/authorization")
    fun authorizationUserController(request: HttpServletRequest, response: HttpServletResponse) {
        val user = request.session.getAttribute("userInfo") as UserDto
        userService.authorization(user)
        response.sendRedirect(clientHomeUrl)
    }

    /**
     * Getting current user from session
     */
    @GetMapping("/user")
    fun getUser(session: HttpSession): ResponseEntity<*> {
        val user = getCurrentUserFromSessionOrNull(session) ?: UserDto()
        return ResponseEntity.ok(user)
    }
}