package web.demo.server.service.api

import web.demo.server.dtos.FileDto
import web.demo.server.entity.File
import web.demo.server.entity.Project

/**
 * Service for operation with [File]
 *
 * @author Alexander Prendota on 2/15/18 JetBrains.
 */
interface FileService {

    fun saveFile(clientId: String, fileDto: FileDto)

    fun deleteFile(publicId: String, projectId: String, clientId: String)

    fun addFile(clientId: String, projectId: String, text: String, name: String)

    fun getFileByPublicId(publicId: String): FileDto

    fun getFileEntityByPublicId(publicId: String): File

    fun isFileExist(publicId: String): Boolean

    fun getAllFileByProjectPublicId(publicId: String): List<FileDto>

    fun renameFile(publicId: String, projectId: String, newName: String, clientId: String)

    fun addFileToProject(project: Project, text: String, name: String): File

    fun convertFileToDto(file: File): FileDto

}