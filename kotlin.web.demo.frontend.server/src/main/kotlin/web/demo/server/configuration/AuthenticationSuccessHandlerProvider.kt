package web.demo.server.configuration

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.common.ProviderPathsConstants
import web.demo.server.controllers.AuthController
import web.demo.server.dtos.UserDto
import web.demo.server.exceptions.AuthorizationProviderException
import web.demo.server.model.ProviderType
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Alexander Prendota on 2/27/18 JetBrains.
 */
class AuthenticationSuccessHandlerProvider : AuthenticationSuccessHandler {

    /**
     * Method is called after successful authorization.
     * Define user provider by request URI[ProviderPathsConstants] and set user details: client id and username to
     * session attribute from [OAuth2Authentication]
     * Redirect to [AuthController] for authorization.
     *
     * @throws [AuthorizationProviderException] when provider is not found
     */
    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val url = request!!.requestURI ?: ""
        val details = (authentication as OAuth2Authentication).userAuthentication.details as LinkedHashMap<*, *>
        when (url) {
            ProviderPathsConstants.FACEBOOK -> request.session.setAttribute(GeneralPathsConstants.CURRENT_USER, getFacebookDetails(details))
            ProviderPathsConstants.GOOGLE   -> request.session.setAttribute(GeneralPathsConstants.CURRENT_USER, getGoogleDetails(details))
            ProviderPathsConstants.STEPIK   -> request.session.setAttribute(GeneralPathsConstants.CURRENT_USER, getStepikDetails(details))
            else -> throw AuthorizationProviderException("No authorization provider detected")
        }
        response!!.sendRedirect("/authorization")
    }

    /**
     * Getting facebook user details: client id and user name
     *
     * @param details - map from [OAuth2Authentication]
     *
     * @return [UserDto]
     */
    private fun getFacebookDetails(details: LinkedHashMap<*, *>): UserDto {
        val clientId = details[ProviderPathsConstants.FACEBOOK_CLIENT_ID]
        val userName = details[ProviderPathsConstants.FACEBOOK_USER_NAME]
        if (clientId != null && userName != null) {
            return UserDto(0, userName.toString(), clientId.toString(), ProviderType.facebook.name)
        }
        throw AuthorizationProviderException("Facebook authorization exception. Can not parse user details")
    }

    /**
     * Getting google user details: client id and user name
     *
     * @param details - map from [OAuth2Authentication]
     *
     * @return [UserDto]
     */
    private fun getGoogleDetails(details: LinkedHashMap<*, *>): UserDto {
        val clientId = details[ProviderPathsConstants.GOOGLE_CLIENT_ID]
        val userName = details[ProviderPathsConstants.GOOGLE_USER_NAME]
        if (clientId != null && userName != null) {
            return UserDto(0, userName.toString(), clientId.toString(), ProviderType.google.name)
        }
        throw AuthorizationProviderException("Google authorization exception. Can not parse user details")
    }

    /**
     * Getting stepik user details: client id and user name
     * Use for authorization only first user.
     * See [AuthPathsConstants] for getting client id and user name
     *
     * @param details - map from [OAuth2Authentication]
     * @return [UserDto]
     */
    private fun getStepikDetails(details: LinkedHashMap<*, *>): UserDto {
        if (details["users"] != null) {
            val listStepicAuthUsers = details["users"] as ArrayList<*>
            if (listStepicAuthUsers.isNotEmpty()) {
                val firstStepicUser = listStepicAuthUsers[0] as LinkedHashMap<*, *>
                val clientId = firstStepicUser[ProviderPathsConstants.STEPIK_CLIENT_ID]
                val name = firstStepicUser[ProviderPathsConstants.STEPIK_USER_NAME]
                if (clientId != null && name != null) {
                    return UserDto(0,name.toString(), clientId.toString(), ProviderType.stepic.name)
                }
            }
        }
        throw AuthorizationProviderException("Stepik authorization exception. Can not parse user details")
    }
}