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
import web.demo.server.repository.ProjectRepository
import web.demo.server.service.api.FileService
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

    @Autowired
    private lateinit var fileService: FileService

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
     * Getting project by publicId
     *
     * @param publicId - id from [Project]
     *
     * @throws [SourceNotFoundException] if project is not exist
     * @return [Project] fields
     */
    override fun getProjectEntityByPublicId(publicId: String): Project {
        val project = projectRepository.findByPublicId(publicId)
        if (project != null) return project
        logger.error("Can not find project by public id. public id: $publicId")
        throw SourceNotFoundException("Can not find project by public id.")
    }

    /**
     * Save the project.
     * Generate publicId by [IdentifierGeneratorService]
     *
     * @param clientId    - [User] id
     * @param projectDto  - [ProjectDto] project for saving
     *
     * @throws [SourceNotFoundException] if user is not exist
     */
    override fun saveProject(clientId: String, projectDto: ProjectDto) {
        val user = userService.defineUser(clientId)
        val nameOfProject = projectDto.name
        checkProjectWithTheSameName(nameOfProject, user)
        val projectId = projectDto.publicId
        val project = getProjectEntityByPublicId(projectId)
        project.args = projectDto.args
        project.confType = projectDto.confType
        project.compilerVersion = projectDto.compilerVersion
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
     * @throws [ValidationException] if user has two projects with the same names
     */
    override fun renameProject(clientId: String, publicId: String, newName: String) {
        val user = userService.defineUser(clientId)
        checkProjectWithTheSameName(newName, user)
        val project = getProjectByPublicIdAndUser(publicId, user)
        project.name = newName
        projectRepository.save(project)
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
        val project = getProjectByPublicIdAndUser(publicId, user)
        projectRepository.delete(project)
    }

    /**
     * Getting project by public Id and [User]
     *
     * @param publicId  - id from [Project]
     * @param user      - [User] entity
     *
     * @throws [SourceNotFoundException] - can not find project by params
     * @return [Project]
     */
    override fun getProjectByPublicIdAndUser(publicId: String, user: User): Project {
        val project = projectRepository.findByPublicIdAndOwnerId(publicId, user)
        if (project != null) return project
        logger.error("Can not find project with id: $publicId and user: ${user.clientId}.")
        throw SourceNotFoundException("Can not find project by public id and owner")
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
     * Add new project with file
     *
     * @param clientId  - [User] id
     * @param name      - project name
     *
     * @throws [SourceNotFoundException] if user is not exist
     * @throws [ValidationException] if user saves more than 100 projects
     *
     * @return [ProjectDto] with files
     */
    override fun addProject(clientId: String, name: String): ProjectDto {
        val user = userService.defineUser(clientId)
        val countOfProjects = projectRepository.countByOwnerId(user)
        if (countOfProjects > 100) throw ValidationException("You can't save more than 100 projects")
        checkProjectWithTheSameName(name, user)
        var project = Project()
        project.name = name
        project.type = GeneralPathsConstants.USER_PROJECT_TYPE
        project.ownerId = user
        project.args = ""
        project.publicId = idGenerator.nextProjectId()
        project.readOnlyFileNames = "[]" // string list of file
        project = projectRepository.save(project)
        val file = fileService.addFileToProject(project, GeneralPathsConstants.FILE_DEFAULT_CONTENT, project.name)
        val projectDto = convertProjectToDto(project)
        projectDto.files = listOf(fileService.convertFileToDto(file))
        return projectDto
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