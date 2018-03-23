package web.demo.server.service.impl

import org.assertj.core.api.Assertions
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.modelmapper.ModelMapper
import web.demo.server.dtos.FileDto
import web.demo.server.entity.File
import web.demo.server.entity.Project
import web.demo.server.entity.User
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.exceptions.ValidationException
import web.demo.server.model.ProviderType
import web.demo.server.repository.FileRepository
import web.demo.server.service.api.ProjectService
import web.demo.server.service.api.UserService

@RunWith(MockitoJUnitRunner::class)
class FileServiceImplTest {

    @Mock
    private lateinit var userService: UserService

    @Mock
    private val modelMapper: ModelMapper = ModelMapper()

    @Mock
    private lateinit var idGenerator: IdentifierGeneratorService

    @Mock
    private lateinit var fileRepository: FileRepository

    @Mock
    private lateinit var projectService: ProjectService

    @InjectMocks
    private lateinit var service: FileServiceImpl

    private lateinit var user: User

    private val USER_ID = "123"
    private val PROJECT_ID = "1234"
    private val FILE_ID = "12345"
    private lateinit var file: File
    private lateinit var project: Project
    private lateinit var fileDto: FileDto

    @Before
    fun init() {
        /*
        * stubs for objects
        */
        user = User()
        user.clientId = USER_ID
        user.provider = ProviderType.google
        user.username = "User"
        user.id = USER_ID.toInt()

        file = File()
        file.text = "text"
        file.name = "filename"
        file.id = FILE_ID.toInt()
        file.publicId = FILE_ID

        project = Project()
        project.name = "Test"
        project.ownerId = user
        project.id = 1234
        project.publicId = PROJECT_ID
        project.compilerVersion = "1.0.0"

        fileDto = FileDto(true, FILE_ID, PROJECT_ID, "text", "filename")

    }

    @Test
    fun getFileByPublicId() {
        Mockito.`when`(fileRepository.findByPublicId(FILE_ID)).thenReturn(file)
        Mockito.`when`(modelMapper.map(file, FileDto::class.java)).thenReturn(fileDto)
        service.getFileByPublicId(FILE_ID)
        Mockito.verify(fileRepository).findByPublicId(FILE_ID)
        Mockito.verify(modelMapper).map(file, FileDto::class.java)
    }

    @Test
    fun getFileByPublicIdThrow() {
        Mockito.`when`(fileRepository.findByPublicId(FILE_ID)).thenReturn(null)
        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.getFileByPublicId(FILE_ID) }
                .withMessageContaining("Can not find file by public id.")

    }

    @Test
    fun getFileEntityByPublicId() {
        Mockito.`when`(fileRepository.findByPublicId(FILE_ID)).thenReturn(file)
        service.getFileEntityByPublicId(FILE_ID)
        Mockito.verify(fileRepository).findByPublicId(FILE_ID)
    }

    @Test
    fun getFileEntityByPublicIdThrow() {
        Mockito.`when`(fileRepository.findByPublicId(FILE_ID)).thenReturn(null)
        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.getFileEntityByPublicId(FILE_ID) }
                .withMessageContaining("Can not find file by public id.")
    }


    @Test
    fun isFileExist() {
        Mockito.`when`(fileRepository.findByPublicId(FILE_ID)).thenReturn(file)
        val fileExist = service.isFileExist(FILE_ID)
        Mockito.verify(fileRepository).findByPublicId(FILE_ID)
        Assert.assertTrue(fileExist)
    }

    @Test
    fun getAllFileByProjectPublicId() {
        Mockito.`when`(projectService.getProjectEntityByPublicId(PROJECT_ID)).thenReturn(project)
        Mockito.`when`(fileRepository.findByProjectId(project)).thenReturn(listOf(file))
        Mockito.`when`(modelMapper.map(file, FileDto::class.java)).thenReturn(fileDto)

        service.getAllFileByProjectPublicId(PROJECT_ID)

        Mockito.verify(modelMapper).map(file, FileDto::class.java)
        Mockito.verify(projectService).getProjectEntityByPublicId(PROJECT_ID)
        Mockito.verify(fileRepository).findByProjectId(project)
    }

    @Test
    fun renameFile() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(fileRepository.findByPublicId(FILE_ID)).thenReturn(file)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.save(file)).thenReturn(file)
        Mockito.`when`(fileRepository.findByProjectIdAndName(project, "NAME.kt")).thenReturn(file)

        service.renameFile(FILE_ID, PROJECT_ID, "NAME", USER_ID)

        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(fileRepository).findByPublicId(FILE_ID)
        Mockito.verify(fileRepository).findByProjectIdAndName(project, "NAME.kt")
        Mockito.verify(fileRepository, Mockito.times(1)).save(file)
    }

    @Test
    fun renameFileThrow() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.findByProjectIdAndName(project, "NAME")).thenReturn(null)


        Assertions.assertThatExceptionOfType(ValidationException::class.java)
                .isThrownBy { service.renameFile(FILE_ID, PROJECT_ID, "NAME", USER_ID) }
                .withMessageContaining("Can not do operation with file. File name already exist")

        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(fileRepository).findByProjectIdAndName(project, "NAME.kt")
    }

    @Test
    fun deleteFile() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.findByPublicIdAndProjectId(FILE_ID, project)).thenReturn(file)
        Mockito.doNothing().`when`(fileRepository).delete(file)

        service.deleteFile(FILE_ID, PROJECT_ID, USER_ID)

        Mockito.verify(fileRepository).findByPublicIdAndProjectId(FILE_ID, project)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
    }

    @Test
    fun deleteFileThrow() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.findByPublicIdAndProjectId(FILE_ID, project)).thenReturn(null)

        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.deleteFile(FILE_ID, PROJECT_ID, USER_ID) }
                .withMessageContaining("Can not delete file in your project. File is not found")

        Mockito.verify(fileRepository).findByPublicIdAndProjectId(FILE_ID, project)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
    }

    @Test
    fun saveFile() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.findByPublicIdAndProjectId(FILE_ID, project)).thenReturn(file)
        Mockito.`when`(fileRepository.save(file)).thenReturn(file)

        service.saveFile(USER_ID, fileDto)

        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
        Mockito.verify(fileRepository).findByPublicIdAndProjectId(FILE_ID, project)
    }

    @Test
    fun addFileToProject() {
        Mockito.`when`(idGenerator.nextFileId()).thenReturn("id_random")
        Mockito.`when`(fileRepository.save(Mockito.any(File::class.java))).thenReturn(file)

        service.addFileToProject(project, "text", "name")

        Mockito.verify(idGenerator, Mockito.times(1)).nextFileId()
        Mockito.verify(fileRepository, Mockito.times(1)).save(Mockito.any(File::class.java))
    }

    @Test
    fun saveFileThrow() {
        val badDto = FileDto(true, FILE_ID, "09876", "text", "filename")
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)

        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.saveFile(USER_ID, badDto) }
                .withMessageContaining("File is not found by public id and project")

        Mockito.verify(userService).defineUser(user.clientId!!)

    }

    @Test
    fun addFile() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.countByProjectId(project)).thenReturn(1)
        Mockito.`when`(fileRepository.findByProjectIdAndName(project, "NAME.kt")).thenReturn(file)
        Mockito.`when`(idGenerator.nextFileId()).thenReturn("id_random")
        Mockito.`when`(fileRepository.save(Mockito.any(File::class.java))).thenReturn(file)

        service.addFile(USER_ID, PROJECT_ID, "text", "NAME")

        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(fileRepository).findByProjectIdAndName(project, "NAME.kt")
        Mockito.verify(idGenerator, Mockito.times(1)).nextFileId()
        Mockito.verify(fileRepository).countByProjectId(project)
        Mockito.verify(fileRepository, Mockito.times(1)).save(Mockito.any(File::class.java))
    }

    @Test
    fun addFileThrow1() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectService.getProjectByPublicIdAndUser(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(fileRepository.countByProjectId(project)).thenReturn(101)

        Assertions.assertThatExceptionOfType(ValidationException::class.java)
                .isThrownBy { service.addFile(USER_ID, PROJECT_ID, "text", "NAME") }
                .withMessageContaining("You can't save more than 100 files")

        Mockito.verify(projectService).getProjectByPublicIdAndUser(PROJECT_ID, user)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(fileRepository).countByProjectId(project)
    }

}