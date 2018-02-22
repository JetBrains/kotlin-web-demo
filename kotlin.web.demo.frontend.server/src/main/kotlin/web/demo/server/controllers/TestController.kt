package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import web.demo.server.service.api.StepikService

/**
 * @author Alexander Prendota on 2/19/18 JetBrains.
 */
@RestController
class TestController {

    @Autowired
    lateinit var stepikService: StepikService

    @GetMapping("/user")
    fun testController(authentication: OAuth2Authentication): OAuth2Authentication {
        var token = (authentication.details as OAuth2AuthenticationDetails).tokenValue
        stepikService.getCourseProgress("4222", token)
        return authentication
    }
}
