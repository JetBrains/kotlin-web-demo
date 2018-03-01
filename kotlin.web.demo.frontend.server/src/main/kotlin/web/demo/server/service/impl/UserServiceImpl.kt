package web.demo.server.service.impl

import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.dtos.UserDto
import web.demo.server.entity.User
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.model.ProviderType
import web.demo.server.repository.UserRepository
import web.demo.server.service.api.UserService

@Service
class UserServiceImpl : UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var modelMapper: ModelMapper

    /**
     * Registration user if not exist
     *
     * @param userDto - user for auth
     *
     * @return authorized [UserDto]
     */
    override fun authorization(userDto: UserDto): UserDto {
        val user = userRepository.findByClientId(userDto.clientId)
        if (user != null) {
            return convertUserToDto(user)
        }
        val newUser = User()
        newUser.clientId = userDto.clientId
        newUser.username = userDto.username
        newUser.provider = ProviderType.valueOf(userDto.provider)
        return convertUserToDto(userRepository.save(newUser))
    }

    /**
     * Define user in storage
     * @param clientId  - client [User] id
     *
     * @throws [SourceNotFoundException] if user is not exist
     */
    override fun defineUser(clientId: String): User {
        val user = userRepository.findByClientId(clientId)
        if (user != null) return user
        logger.error("Can not rename project 'cause user is not found. User: $clientId")
        throw SourceNotFoundException("Can not rename project 'cause user is not found")
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