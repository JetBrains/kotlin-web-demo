package web.demo.server.service.api

import web.demo.server.dtos.ProjectDto
import web.demo.server.entity.Project
import web.demo.server.entity.User

/**
 * @author Alexander Prendota on 2/28/18 JetBrains.
 */
interface ProjectService {

    fun getAllProjectByUser(clientId: String): List<ProjectDto>

    fun isExistProject(publicId: String): Boolean

    fun getProjectByPublicId(publicId: String): ProjectDto

    fun getProjectEntityByPublicId(publicId: String): Project

    fun renameProject(clientId: String, publicId: String, newName: String)

    fun deleteProject(clientId: String, publicId: String)

    fun saveProject(clientId: String, projectDto: ProjectDto)

    fun getProjectByPublicIdAndUser(publicId: String, user: User): Project

}