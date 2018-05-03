package web.demo.server.controllers

import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.configuration.AuthenticationSuccessHandlerProvider
import web.demo.server.dtos.UserDto
import web.demo.server.exceptions.AuthorizationProviderException
import web.demo.server.model.ProviderType
import javax.servlet.http.HttpSession


/**
 * @author Alexander Prendota on 3/19/18 JetBrains.
 */
interface BaseController {

    /**
     * Getting token from request
     * Needed for synchronizing the user course information
     * @see <a href="https://stepik.org">Stepik Course</a>
     * @param authentication - for getting token for request to stepik
     * @param session        - for getting info about user
     *
     * @throws [UnsupportedOperationException] - if provider is not [ProviderType.stepik]
     */
    fun getStepikToken(authentication: OAuth2Authentication, session: HttpSession): String {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as? UserDto
        if (user != null && user.provider != ProviderType.stepik.name)
            throw UnsupportedOperationException("Can not get course materials for ${user.provider} provider. Use Stepik authentication.")
        return (authentication.details as OAuth2AuthenticationDetails).tokenValue
    }

    /**
     * Get current user from [HttpSession]
     * Use attribute [GeneralPathsConstants.CURRENT_USER]
     * Set this attribute after successfully authentication in [AuthenticationSuccessHandlerProvider]
     *
     * @throws [AuthorizationProviderException] if no attribute in session
     */
    fun getCurrentUserFromSession(session: HttpSession): UserDto {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as? UserDto
        return user ?: throw AuthorizationProviderException("No user in current session detected")
    }

    /**
     * Get current user from [HttpSession] or null if not exist
     * Use attribute [GeneralPathsConstants.CURRENT_USER]
     * Set this attribute after successfully authentication in [AuthenticationSuccessHandlerProvider]
     */
    fun getCurrentUserFromSessionOrNull(session: HttpSession): UserDto? {
        return session.getAttribute(GeneralPathsConstants.CURRENT_USER) as? UserDto
    }

}