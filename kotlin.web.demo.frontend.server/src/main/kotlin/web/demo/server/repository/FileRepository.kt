package web.demo.server.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import web.demo.server.entity.File
import web.demo.server.entity.Project

/**
 * @author Alexander Prendota on 2/8/18 JetBrains.
 */
@Repository
interface FileRepository : CrudRepository<File, Int> {

    fun findByPublicId(publicId: String): File?

    fun findByProjectId(projectId: Project): List<File>?

    fun findByPublicIdAndProjectId(publicId: String, projectId: Project): File?

    fun findByProjectIdAndName(projectId: Project, name: String): File?

    fun countByProjectId(projectId: Project): Int

}