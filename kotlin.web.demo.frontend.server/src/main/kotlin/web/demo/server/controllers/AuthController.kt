package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import web.demo.server.service.api.AuthService
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * @author Alexander Prendota on 2/19/18 JetBrains.
 */
@Controller
class AuthController {

    @Autowired
    lateinit var authService: AuthService

    /**
     * Logout user from web-demo
     */
    @GetMapping("/logout")
    fun exit(request: HttpServletRequest, response: HttpServletResponse) {
        SecurityContextLogoutHandler().logout(request, null, null)
        try {
            response.sendRedirect(request.getHeader("referer"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Authorization user in web-demo.
     * Define user provider.
     */
    @GetMapping("/authorization")
    fun authorizationUserController(authentication: OAuth2Authentication): ResponseEntity<*> {
        val details = authentication.userAuthentication.details
        return ResponseEntity.ok(authService.authorizationUser(details))
    }
}