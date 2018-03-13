package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.dtos.UserDto
import web.demo.server.dtos.stepik.ProgressDto
import web.demo.server.model.ProviderType
import web.demo.server.service.api.StepikService
import javax.servlet.http.HttpSession

/**
 * @author Alexander Prendota on 3/13/18 JetBrains.
 */
@RestController
@RequestMapping(GeneralPathsConstants.API_EDU)
class EducationController {

    @Autowired
    lateinit var stepikService: StepikService

    /**
     * Getting personal course progress
     * Work only [ProviderType.stepik]
     * @param authentication - for getting token for request to stepik
     * @param session        - for getting info about user
     *
     * @throws [UnsupportedOperationException] - if provider is not [ProviderType.stepik]
     * @return list of [ProgressDto]
     */
    @GetMapping("${GeneralPathsConstants.PROGRESS}/{id}")
    fun getUserCourseProgress(authentication: OAuth2Authentication, session: HttpSession,
                       @PathVariable id: String): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        if (user.provider != ProviderType.stepik.name)
            throw UnsupportedOperationException("Can not get course progress for ${user.provider} provider")
        val token = (authentication.details as OAuth2AuthenticationDetails).tokenValue
        val progress = stepikService.getCourseProgress(id, token)
        return ResponseEntity.ok(progress)
    }


    /**
     * Getting loaded Stepic courses.
     * See available course from application.yml
     */
    @GetMapping(GeneralPathsConstants.COURSES)
    fun getCourses(): ResponseEntity<*> {
        return ResponseEntity.ok(stepikService.getCourses())
    }
}