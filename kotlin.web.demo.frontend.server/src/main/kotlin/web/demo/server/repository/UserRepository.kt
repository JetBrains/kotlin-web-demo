package web.demo.server.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import web.demo.server.entity.User

/**
 * @author Alexander Prendota on 2/6/18 JetBrains.
 */
@Repository
interface UserRepository: CrudRepository<User, Int> {
    fun findByClientId(clientId: String): User
}