package web.demo.server.service.impl

import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import web.demo.server.dtos.FileDto
import web.demo.server.entity.File
import web.demo.server.entity.Project
import web.demo.server.entity.User
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.exceptions.ValidationException
import web.demo.server.repository.FileRepository
import web.demo.server.service.api.FileService
import web.demo.server.service.api.ProjectService
import web.demo.server.service.api.UserService

/**
 * @author Alexander Prendota on 2/15/18 JetBrains.
 */
@Service
class FileServiceImpl : FileService {

    private val logger = LoggerFactory.getLogger(FileServiceImpl::class.java)

    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    private lateinit var modelMapper: ModelMapper

    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var idGenerator: IdentifierGeneratorService

    /**
     * Getting file by publicId
     *
     * @param publicId - id from [File]
     *
     * @throws [SourceNotFoundException] if file is not exist
     * @return [FileDto] with [File] fields
     */
    override fun getFileByPublicId(publicId: String): FileDto {
        val file = fileRepository.findByPublicId(publicId)
        if (file != null) return convertFileToDto(file)
        logger.error("Can not find file by public id. public id: $publicId")
        throw SourceNotFoundException("Can not find file by public id.")
    }

    /**
     * Check Is the file exist
     *
     * @param publicId - id from [File]
     *
     * @return true if file exists
     */
    override fun isFileExist(publicId: String): Boolean {
        val file = fileRepository.findByPublicId(publicId)
        return file != null
    }

    /**
     * Getting list of [FileDto] by [Project]
     *
     * @param publicId - id from [Project]
     *
     * @throws [SourceNotFoundException] if project is not exist
     * @return list of [FileDto]
     */
    override fun getAllFileByProjectPublicId(publicId: String): List<FileDto> {
        val project = projectService.getProjectEntityByPublicId(publicId)
        val files = fileRepository.findByProjectId(project)
                .orEmpty().map { convertFileToDto(it) }
        return files
    }

    /**
     * Rename file
     *
     * @param publicId  - id from [File]
     * @param projectId - id from [Project]
     * @param newName   - new file name
     * @param clientId  - id from [User]
     *
     * @throws [SourceNotFoundException]    - Can not find file, project, user
     * @throws [ValidationException]        - name already exist
     */
    override fun renameFile(publicId: String, projectId: String, newName: String, clientId: String) {
        val user = userService.defineUser(clientId)
        val project = projectService.getProjectByPublicIdAndUser(projectId, user)
        checkFileWithTheSameName(project, newName)
        val file = fileRepository.findByPublicId(publicId)
        if (file != null) {
            file.name = validateFileName(newName)
            fileRepository.save(file)
        } else {
            logger.error("Can not rename file in your project. File is not found.Project id: $projectId, file id: $publicId, name: $clientId")
            throw SourceNotFoundException("Can not rename file in your project. File is not found")
        }
    }

    /**
     * Delete file
     *
     * @param publicId  - id from [File]
     * @param projectId - id from [Project]
     * @param clientId  - id from [User]
     *
     * @throws [SourceNotFoundException]    - Can not find file, project, user
     */
    override fun deleteFile(publicId: String, projectId: String, clientId: String) {
        val user = userService.defineUser(clientId)
        val project = projectService.getProjectByPublicIdAndUser(projectId, user)
        val file = fileRepository.findByPublicIdAndProjectId(publicId, project)
        if (file != null) {
            fileRepository.delete(file)
        } else {
            throw SourceNotFoundException("Can not delete file in your project. File is not found")
        }

    }

    /**
     * Add file
     *
     * @param text      - file content
     * @param name      - name of file
     * @param projectId - id from [Project]
     * @param clientId  - id from [User]
     *
     * @throws [SourceNotFoundException] - Can not find file, project, user
     * @throws [ValidationException]     - if count of file in project more than 100
     */
    override fun addFile(clientId: String, projectId: String?, text: String?, name: String?) {
        val user = userService.defineUser(clientId)
        if (projectId != null) {
            val project = projectService.getProjectByPublicIdAndUser(projectId, user)
            val countFiles = fileRepository.countByProjectId(project)
            if (countFiles > 100) throw ValidationException("You can't save more than 100 projects")
            if (text != null && name != null) {
                checkFileWithTheSameName(project, name)
                addFileToProject(project, text, name)
            } else {
                logger.error("Can not add file. Empty parameters — name: $name, text: $text ")
                throw SourceNotFoundException("Can not add file. Empty parameters")
            }
        } else {
            logger.error("Can not add file. Project: $projectId is not found ")
            throw SourceNotFoundException("Can not add file. Project is not found")
        }
    }

    /**
     * Save file to [Project]
     *
     * @param project - project
     * @param text    - file content
     * @param name    - name of file
     */
    override fun addFileToProject(project: Project, text: String, name: String) {
        val file = File()
        file.name = validateFileName(name)
        file.projectId = project
        file.publicId = idGenerator.nextFileId()
        file.text = text
        fileRepository.save(file)
    }

    override fun saveFile() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Find file with the same name in project
     *
     * @param project - [Project]
     * @param name    - name of file
     */
    private fun checkFileWithTheSameName(project: Project, name: String) {
        val newNameOfFile = validateFileName(name)
        val fileWithTheSameName = fileRepository.findByProjectIdAndName(project, newNameOfFile)
        if (fileWithTheSameName != null) throw ValidationException("Can not do operation with file. File name already exist")
    }

    /**
     * Add postfix .kt to file name
     * @param name - string file name
     */
    private fun validateFileName(name: String): String {
        return if (name.endsWith(".kt")) name else "$name.kt"
    }

    /**
     * Converter from [File] to [FileDto]
     *
     * @param file - [File]
     * @return [FileDto]
     */
    private fun convertFileToDto(file: File): FileDto {
        return modelMapper.map(file, FileDto::class.java)
    }

}