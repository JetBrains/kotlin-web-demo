package web.demo.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.dtos.FileDto
import web.demo.server.dtos.UserDto
import web.demo.server.entity.File
import web.demo.server.entity.Project
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.exceptions.ValidationException
import web.demo.server.service.api.FileService
import javax.servlet.http.HttpSession

/**
 * Controller for operation with [File]
 *
 * @author Alexander Prendota on 2/27/18 JetBrains.
 */
@RestController
@RequestMapping(GeneralPathsConstants.API_FILE)
class FileController {

    @Autowired
    lateinit var fileService: FileService

    /**
     * Getting project details [File] by public id
     *
     * @param publicId  - id project from [File]
     * @throws [SourceNotFoundException] if file is not found
     */
    @GetMapping()
    fun getFileByPublicId(@RequestParam("publicId") publicId: String): ResponseEntity<*> {
        val file = fileService.getFileByPublicId(publicId)
        return ResponseEntity.ok(file)
    }

    /**
     * Check file in storage
     * @param publicId  - id project from [File]
     */
    @GetMapping(GeneralPathsConstants.EXIST)
    fun isFileExist(@RequestParam("publicId") publicId: String): ResponseEntity<*> {
        val isFileExist = fileService.isFileExist(publicId)
        return ResponseEntity.ok(isFileExist)
    }

    /**
     * Getting list of [FileDto] by [Project]
     *
     * @param publicId - id from [Project]
     *
     * @throws [SourceNotFoundException] if project is not exist
     * @return list of [FileDto]
     */
    @GetMapping(GeneralPathsConstants.ALL)
    fun getAllFilesByProjectId(@RequestParam("publicId") publicId: String): ResponseEntity<*> {
        val files = fileService.getAllFileByProjectPublicId(publicId)
        return ResponseEntity.ok(files)
    }

    /**
     * Rename file
     *
     * @param publicId  - id from [File]
     * @param projectId - id from [Project]
     * @param newName   - new file name
     * @param session   - for getting info about user
     *
     * @throws [SourceNotFoundException]    - Can not find file, project, user
     * @throws [ValidationException]        - name already exist
     */
    @PostMapping(GeneralPathsConstants.RENAME)
    fun renameFile(session: HttpSession,
                   @RequestParam("publicId") publicId: String,
                   @RequestParam("projectId") projectId: String,
                   @RequestParam("newName") newName: String): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        fileService.renameFile(publicId, projectId, newName, user.clientId)
        return ResponseEntity.ok("File $newName renamed successfully")
    }

    /**
     * Delete file
     *
     * @param publicId  - id from [File]
     * @param projectId - id from [Project]
     * @param session   - for getting info about user
     *
     * @throws [SourceNotFoundException]    - Can not find file, project, user
     */
    @DeleteMapping(GeneralPathsConstants.DELETE)
    fun deleteFile(session: HttpSession,
                   @RequestParam("publicId") publicId: String,
                   @RequestParam("projectId") projectId: String): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        fileService.deleteFile(publicId, projectId, user.clientId)
        return ResponseEntity.ok("File was deleted successfully")
    }

    /**
     * Add file
     *
     * @param file - file with fields
     * @param session   - for getting info about user
     *
     * @throws [SourceNotFoundException] - Can not find file, project, user
     * @throws [ValidationException]     - if count of file in project more than 100
     */
    @PostMapping(GeneralPathsConstants.ADD)
    fun addFile(session: HttpSession,
                @RequestBody file: FileDto): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        fileService.addFile(user.clientId, file.projectId, file.text, file.name)
        return ResponseEntity.ok("File was added successfully ")
    }

    @PostMapping(GeneralPathsConstants.SAVE)
    fun saveFile(session: HttpSession,
                 @RequestBody file: FileDto): ResponseEntity<*> {
        val user = session.getAttribute(GeneralPathsConstants.CURRENT_USER) as UserDto
        return ResponseEntity.ok("File ${file.name} saved successfully")
    }


}