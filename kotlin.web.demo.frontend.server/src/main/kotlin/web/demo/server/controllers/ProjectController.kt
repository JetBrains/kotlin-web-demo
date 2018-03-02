package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.dtos.ProjectDto
import web.demo.server.dtos.UserDto
import web.demo.server.entity.Project
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.exceptions.ValidationException
import web.demo.server.service.api.ProjectService
import javax.servlet.http.HttpSession

/**
 * @author Alexander Prendota on 2/28/18 JetBrains.
 */
@RestController
@RequestMapping(GeneralPathsConstants.API_PROJECT)
class ProjectController {

    @Autowired
    lateinit var projectService: ProjectService

    /**
     * Getting project details [Project] by public id
     *
     * @param publicId  - id project from [Project]
     * @throws [SourceNotFoundException] if project is not found
     */
    @GetMapping
    fun getProjectByPublicId(@RequestParam("publicId") publicId: String): ResponseEntity<*> {
        val projectDto = projectService.getProjectByPublicId(publicId)
        return ResponseEntity.ok(projectDto)
    }

    /**
     * Getting all user projects
     *
     * @param session   - for getting info about user
     */
    @GetMapping(GeneralPathsConstants.ALL)
    fun getAllUserProjects(session: HttpSession): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        val projects = projectService.getAllProjectByUser(user.clientId)
        return ResponseEntity.ok(projects)
    }

    /**
     * Check project in storage
     * @param publicId  - id project from [Project]
     */
    @GetMapping(GeneralPathsConstants.EXIST)
    fun projectExist(@RequestParam("publicId") publicId: String): ResponseEntity<*> {
        val isProjectExist = projectService.isExistProject(publicId)
        return ResponseEntity.ok(isProjectExist)
    }

    /**
     * Rename project name
     *
     * @param session   - for getting info about user
     * @param publicId  - id project from [Project]
     * @param newName   - new project name
     *
     * @throws [SourceNotFoundException] if user is not exist or project is not found
     */
    @PostMapping(GeneralPathsConstants.RENAME)
    fun renameProject(session: HttpSession,
                      @RequestParam("publicId") publicId: String,
                      @RequestParam("newName") newName: String): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        projectService.renameProject(user.clientId, publicId, newName)
        return ResponseEntity.ok("Project $newName renamed successfully")
    }

    /**
     * Delete project
     *
     * @param session   - for getting info about user
     * @param publicId  - id project from [Project]
     *
     * @throws [SourceNotFoundException] if user is not exist or project is not found
     */
    @DeleteMapping(GeneralPathsConstants.DELETE)
    fun deleteProject(session: HttpSession,
                      @RequestParam("publicId") publicId: String): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        projectService.deleteProject(user.clientId, publicId)
        return ResponseEntity.ok("Project was deleted successfully")
    }

    /**
     * Save Project to storage
     *
     * @param session    - for getting info about user
     * @param projectDto - project for saving
     *
     * @throws [SourceNotFoundException] if user is not exist
     * @throws [ValidationException] if user has two projects with the same names
     */
    @PostMapping(GeneralPathsConstants.SAVE)
    fun saveProject(session: HttpSession,
                    @RequestBody projectDto: ProjectDto): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        projectService.saveProject(user.clientId, projectDto)
        return ResponseEntity.ok("Project ${projectDto.name} saved successfully")
    }

    /**
     * Add new project with file
     *
     * @param session   - for getting info about user
     * @param name      - project name
     *
     * @throws [SourceNotFoundException] if user is not exist
     * @throws [ValidationException] if user has two projects with the same names
     *
     * @return [ProjectDto] with files
     */
    @PostMapping(GeneralPathsConstants.ADD)
    fun addProject(session: HttpSession,
                   @RequestParam("name") name: String): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        val project = projectService.addProject(user.clientId, name)
        return ResponseEntity.ok(project)

    }

}