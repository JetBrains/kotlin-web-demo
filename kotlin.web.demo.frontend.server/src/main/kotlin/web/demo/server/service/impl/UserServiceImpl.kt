package web.demo.server.service.impl

import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.dtos.UserDto
import web.demo.server.entity.User
import web.demo.server.model.ProviderType
import web.demo.server.repository.UserRepository
import web.demo.server.service.api.UserService

@Service
class UserServiceImpl : UserService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var modelMapper: ModelMapper

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
     * Converter from [User] to [UserDto]
     *
     * @param user - [User]
     * @return [UserDto]
     */
    private fun convertUserToDto(user: User): UserDto {
        return modelMapper.map(user, UserDto::class.java)
    }

}