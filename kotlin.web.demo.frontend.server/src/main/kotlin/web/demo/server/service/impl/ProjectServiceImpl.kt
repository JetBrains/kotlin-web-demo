package web.demo.server.service.impl

import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.dtos.ProjectDto
import web.demo.server.entity.Project
import web.demo.server.entity.User
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.exceptions.ValidationException
import web.demo.server.model.ConfType
import web.demo.server.repository.ProjectRepository
import web.demo.server.service.api.ProjectService
import web.demo.server.service.api.UserService

@Service
class ProjectServiceImpl : ProjectService {

    private val logger = LoggerFactory.getLogger(ProjectServiceImpl::class.java)

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var modelMapper: ModelMapper

    @Autowired
    private lateinit var idGenerator: IdentifierGeneratorService

    /**
     * Getting all user projects
     *
     * @param clientId - id from [User]
     *
     * @throws [SourceNotFoundException] if user is not exist
     * @return list of [ProjectDto]
     */
    override fun getAllProjectByUser(clientId: String): List<ProjectDto> {
        val user = userService.defineUser(clientId)
        val userProjects = projectRepository.findByOwnerId(user)
                .orEmpty()
                .map { convertProjectToDto(it) }.toList()
        return userProjects
    }

    /**
     * Check Is the project exist
     * @param publicId - id from [Project]
     *
     * @return true if project exists
     */
    override fun isExistProject(publicId: String): Boolean {
        val project = projectRepository.findByPublicId(publicId)
        return project != null
    }

    /**
     * Getting project by publicId
     *
     * @param publicId - id from [Project]
     *
     * @throws [SourceNotFoundException] if project is not exist
     * @return [ProjectDto] with [Project] fields
     */
    override fun getProjectByPublicId(publicId: String): ProjectDto {
        val project = projectRepository.findByPublicId(publicId)
        if (project != null) return convertProjectToDto(project)
        logger.error("Can not find project by public id. public id: $publicId")
        throw SourceNotFoundException("Can not find project by public id.")
    }

    /**
     * Save the project.
     * Generate publicId by [IdentifierGeneratorService]
     *
     * @param clientId  - [User] id
     * @param projectDto  - [ProjectDto] project for saving
     *
     * @throws [SourceNotFoundException] if user is not exist
     * @throws [ValidationException] if user has two projects with the same names
     */
    override fun saveProject(clientId: String, projectDto: ProjectDto) {
        val user = userService.defineUser(clientId)
        val nameOfProject = projectDto.name ?: GeneralPathsConstants.DEFAULT_PROJECT_NAME
        checkProjectWithTheSameName(nameOfProject, user)
        val id = idGenerator.nextProjectId()
        val project = Project()
        project.publicId = id
        project.ownerId = user
        project.name = nameOfProject
        project.args = projectDto.args ?: ""
        project.confType = ConfType.valueOf(projectDto.confType ?: "java")
        project.compilerVersion = projectDto.compilerVersion
        project.originUrl = projectDto.originUrl
        project.type = GeneralPathsConstants.USER_PROJECT_TYPE
        project.readOnlyFileNames = projectDto.readOnlyFileNames.toString()
        projectRepository.save(project)
    }

    /**
     * Change the name of the project
     *
     * @param clientId  - [User] id
     * @param publicId  - id from [Project]
     * @param newName   - new name
     *
     * @throws [SourceNotFoundException] if project or user is not exist
     */
    override fun renameProject(clientId: String, publicId: String, newName: String) {
        val user = userService.defineUser(clientId)
        checkProjectWithTheSameName(newName, user)
        val project = projectRepository.findByPublicIdAndOwnerId(publicId, user)
        if (project != null) {
            project.name = newName
            projectRepository.save(project)
            return
        }
        logger.error("Can not rename project 'cause project is not found. public id: $publicId")
        throw SourceNotFoundException("Can not rename project 'cause project is not found")

    }

    /**
     * Delete user project
     *
     * @param clientId  - [User] id
     * @param publicId  - id from [Project]
     *
     * @throws [SourceNotFoundException] if project or user is not exist
     */
    override fun deleteProject(clientId: String, publicId: String) {
        val user = userService.defineUser(clientId)
        val project = projectRepository.findByPublicIdAndOwnerId(publicId, user)
        if (project != null) {
            projectRepository.delete(project)
            return
        }
        logger.error("Can not delete project 'cause project is not found. Client: $clientId, Project: $project")
        throw SourceNotFoundException("Can not delete project 'cause project is not found")
    }

    /**
     * Check name of project in storage
     *
     * @throws [ValidationException] if user has two projects with the same names
     */
    private fun checkProjectWithTheSameName(name: String, user: User) {
        projectRepository.findByNameAndOwnerId(name, user) ?: return
        logger.error("Can not validate project with id: ${user.clientId} and name: $name. Project name already exist")
        throw ValidationException("Can not validate project with id and name. Project name already exist")
    }

    /**
     * Converter from [Project] to [ProjectDto]
     *
     * @param project - [Project]
     * @return [ProjectDto]
     */
    private fun convertProjectToDto(project: Project): ProjectDto {
        return modelMapper.map(project, ProjectDto::class.java)
    }

}