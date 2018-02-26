package web.demo.server.controllers

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * @author Alexander Prendota on 2/19/18 JetBrains.
 */
@Controller
class AuthController {
    @GetMapping("/logout")
    fun exit(request: HttpServletRequest, response: HttpServletResponse) {
        SecurityContextLogoutHandler().logout(request, null, null)
        try {
            response.sendRedirect(request.getHeader("referer"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}