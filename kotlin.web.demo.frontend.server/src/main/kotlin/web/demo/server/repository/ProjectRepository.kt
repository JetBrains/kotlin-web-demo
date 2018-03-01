package web.demo.server.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import web.demo.server.entity.Project
import web.demo.server.entity.User

/**
 * @author Alexander Prendota on 2/8/18 JetBrains.
 */
@Repository
interface ProjectRepository : CrudRepository<Project, Int> {
    fun findByPublicId(publicId: String): Project?
    fun findByPublicIdAndOwnerId(publicId: String, ownerId: User): Project?
    fun findByNameAndOwnerId(name: String, ownerId: User): Project?
    fun findByOwnerId(ownerId: User): List<Project>?
}