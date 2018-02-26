package web.demo.server.service.impl

import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.stereotype.Service
import web.demo.server.common.AuthConstants
import web.demo.server.dtos.UserDto
import web.demo.server.entity.User
import web.demo.server.exceptions.AuthorizationProviderException
import web.demo.server.model.ProviderType
import web.demo.server.repository.UserRepository
import web.demo.server.service.api.AuthService

/**
 * Implementation of [AuthService]
 * Service for authorization user.
 * Define user provider.
 */
@Service
class AuthServiceImpl : AuthService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var modelMapper: ModelMapper

    private val logger: Logger = LoggerFactory.getLogger(AuthServiceImpl::class.java)

    /**
     * Define the auth provider.
     * See [AuthConstants] for getting client id and user name from [OAuth2Authentication] object
     *
     * @param detail - object with client detail map
     *
     * @return authenticated user [UserDto]
     */
    override fun authorizationUser(detail: Any): UserDto {
        if (detail is LinkedHashMap<*, *>) {
            val name: String
            val clientId: String
            if (detail[AuthConstants.GOOGLE_CLIENT_ID] != null && detail[AuthConstants.GOOGLE_USER_NAME] != null) {
                clientId = detail[AuthConstants.GOOGLE_CLIENT_ID] as String
                name = detail[AuthConstants.GOOGLE_USER_NAME] as String
                return registrationGoogleUser(clientId, name)
            } else if (detail[AuthConstants.FACEBOOK_CLIENT_ID] != null && detail[AuthConstants.FACEBOOK_USER_NAME] != null) {
                clientId = detail[AuthConstants.FACEBOOK_CLIENT_ID] as String
                name = detail[AuthConstants.FACEBOOK_USER_NAME] as String
                return registrationFacebookUser(clientId, name)
            } else if (detail["users"] != null) {
                val listStepicAuthUsers = detail["users"] as ArrayList<*>
                return registrationStepicUser(listStepicAuthUsers)
            }
        }
        logger.error("No authorization provider detected")
        throw AuthorizationProviderException("No authorization provider detected")
    }

    /**
     * Registration user if not exist
     *
     * @return authenticated user [UserDto]
     */
    private fun registrationGoogleUser(clientId: String, name: String): UserDto {
        return saveUser(clientId, name, ProviderType.google)

    }

    /**
     * Registration user if not exist
     *
     * @return authenticated user [UserDto]
     */
    private fun registrationFacebookUser(clientId: String, name: String): UserDto {
        return saveUser(clientId, name, ProviderType.facebook)
    }

    /**
     * Registration user if not exist.
     * Getting list of users from Stepik details.
     * Use for authorization only first user.
     * See [AuthConstants] for getting client id and user name
     *
     * @return authenticated user [UserDto]
     */
    private fun registrationStepicUser(listOfAuthUsers: ArrayList<*>): UserDto {
        if (listOfAuthUsers.isNotEmpty()) {
            val firstStepicUser = listOfAuthUsers[0] as LinkedHashMap<*, *>
            val clientId = firstStepicUser[AuthConstants.STEPIK_CLIENT_ID] as Int?
            val name = firstStepicUser[AuthConstants.STEPIK_USER_NAME] as String?
            if (clientId != null && name != null) {
                return saveUser(clientId.toString(), name, ProviderType.stepic)
            }
        }
        logger.error("No users from STEPIK authorization")
        throw AuthorizationProviderException("No users from STEPIK auth")
    }


    private fun registrationGitHubUser(): UserDto {
        TODO("not implemented")
    }

    private fun registrationTwitterUser(): UserDto {
        TODO("not implemented")
    }

    /**
     * Save and return new user or return existing user
     *
     * @param clientId - client form provider
     * @param name     - full name of user
     * @param provider - kind of [ProviderType]
     *
     * @return [UserDto]
     */
    private fun saveUser(clientId: String, name: String, provider: ProviderType): UserDto {
        val user = userRepository.findByClientId(clientId)
        if (user != null) {
            return convertUserToDto(user)
        }
        val newUser = User()
        newUser.clientId = clientId
        newUser.username = name
        newUser.provider = provider
        return convertUserToDto(userRepository.save(newUser))
    }

    /**
     * Converter from [User] to [UserDto]
     *
     * @param user - [User]
     * @return [UserDto]
     */
    private fun convertUserToDto(user: User): UserDto {
        return modelMapper.map(user, UserDto::class.java)
    }

}