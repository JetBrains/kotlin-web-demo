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
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.dtos.ProjectDto
import web.demo.server.entity.File
import web.demo.server.entity.Project
import web.demo.server.entity.User
import web.demo.server.exceptions.SourceNotFoundException
import web.demo.server.exceptions.ValidationException
import web.demo.server.model.ConfType
import web.demo.server.model.ProjectType
import web.demo.server.model.ProviderType
import web.demo.server.repository.ProjectRepository
import web.demo.server.service.api.FileService
import web.demo.server.service.api.UserService


@RunWith(MockitoJUnitRunner::class)
class ProjectServiceImplTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var userService: UserService

    @Mock
    private val modelMapper: ModelMapper = ModelMapper()

    @Mock
    private lateinit var idGenerator: IdentifierGeneratorService

    @Mock
    private lateinit var fileService: FileService

    @InjectMocks
    private lateinit var service: ProjectServiceImpl

    private lateinit var project: Project
    private lateinit var user: User
    private lateinit var projectDto: ProjectDto
    private val PROJECT_ID = "1234"
    private val USER_ID = "123"

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

        project = Project()
        project.name = "Test"
        project.ownerId = user
        project.id = 1234
        project.publicId = PROJECT_ID
        project.compilerVersion = "1.0.0"

        projectDto = ProjectDto(1234, "Test", "", ConfType.java,
                PROJECT_ID, ProjectType.USER_PROJECT, "", "1.0.0", emptyList(), emptyList())
    }

    @Test
    fun isExistProject() {
        Mockito.`when`(projectRepository.findByPublicId(PROJECT_ID)).thenReturn(project)
        val existProject = service.isExistProject(PROJECT_ID)

        Mockito.verify(projectRepository).findByPublicId(PROJECT_ID)

        Assert.assertTrue(existProject)
    }


    @Test
    fun getProjectEntityByPublicId() {
        Mockito.`when`(projectRepository.findByPublicId(PROJECT_ID)).thenReturn(project)
        val entity = service.getProjectEntityByPublicId(PROJECT_ID)

        Mockito.verify(projectRepository).findByPublicId(PROJECT_ID)

        Assert.assertEquals("Test", entity.name)
        Assert.assertEquals(PROJECT_ID, entity.publicId)
    }

    @Test
    fun getProjectEntityByPublicIdThrow() {
        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.getProjectEntityByPublicId("09828378") }
                .withMessageContaining("Can not find project by public id.")
    }

    @Test
    fun getProjectByPublicId() {
        Mockito.`when`(projectRepository.findByPublicId(PROJECT_ID)).thenReturn(project)
        Mockito.`when`(modelMapper.map(project, ProjectDto::class.java)).thenReturn(projectDto)

        val dto = service.getProjectByPublicId(PROJECT_ID)

        Mockito.verify(projectRepository).findByPublicId(PROJECT_ID)

        Assert.assertEquals("Test", dto.name)
        Assert.assertEquals(PROJECT_ID, dto.publicId)
    }

    @Test
    fun getProjectByPublicIdThrow() {
        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.getProjectByPublicId("09828378") }
                .withMessageContaining("Can not find project by public id.")
    }


    @Test
    fun getAllProjectByUser() {
        Mockito.`when`(projectRepository.findByOwnerId(user)).thenReturn(listOf(project))
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(modelMapper.map(project, ProjectDto::class.java)).thenReturn(projectDto)

        val projects = service.getAllProjectByUser(USER_ID)

        Mockito.verify(projectRepository).findByOwnerId(user)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Assert.assertTrue(projects.isNotEmpty())
        Assert.assertEquals("Test", projects[0].name)

    }

    @Test
    fun getProjectByPublicIdAndUser() {
        Mockito.`when`(projectRepository.findByPublicIdAndOwnerId(PROJECT_ID, user)).thenReturn(project)

        val result = service.getProjectByPublicIdAndUser(PROJECT_ID, user)

        Mockito.verify(projectRepository).findByPublicIdAndOwnerId(PROJECT_ID, user)
        Assert.assertEquals("Test", result.name)
        Assert.assertEquals(PROJECT_ID, result.publicId)

    }

    @Test
    fun getProjectByPublicIdAndUserThrow() {
        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.getProjectByPublicIdAndUser("123", user) }
                .withMessageContaining("Can not find project by public id and owner")
    }

    @Test
    fun deleteProject() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.findByPublicIdAndOwnerId(PROJECT_ID, user)).thenReturn(project)
        Mockito.doNothing().`when`(projectRepository).delete(project)

        service.deleteProject(USER_ID, PROJECT_ID)

        Mockito.verify(projectRepository).findByPublicIdAndOwnerId(PROJECT_ID, user)
        Mockito.verify(projectRepository, Mockito.times(1)).delete(project)
        Mockito.verify(userService).defineUser(user.clientId!!)
    }

    @Test
    fun renameProject() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.findByPublicIdAndOwnerId(PROJECT_ID, user)).thenReturn(project)
        Mockito.`when`(projectRepository.findByNameAndOwnerId("RENAME", user)).thenReturn(null)
        Mockito.`when`(projectRepository.save(project)).thenReturn(project)

        service.renameProject(USER_ID, PROJECT_ID, "RENAME")

        Mockito.verify(projectRepository).findByPublicIdAndOwnerId(PROJECT_ID, user)
        Mockito.verify(projectRepository, Mockito.times(1)).save(project)
        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(projectRepository).findByNameAndOwnerId("RENAME", user)
    }

    @Test
    fun renameProjectThrow() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.findByNameAndOwnerId("RENAME", user)).thenReturn(project)

        Assertions.assertThatExceptionOfType(ValidationException::class.java)
                .isThrownBy { service.renameProject(USER_ID, PROJECT_ID, "RENAME") }
                .withMessageContaining("Can not validate project with id and name. Project name already exist")

    }


    @Test
    fun saveProject() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.findByNameAndOwnerId("Test", user)).thenReturn(null)
        Mockito.`when`(projectRepository.save(project)).thenReturn(project)
        Mockito.`when`(projectRepository.findByPublicId(PROJECT_ID)).thenReturn(project)

        service.saveProject(USER_ID, projectDto)

        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(projectRepository, Mockito.times(1)).save(project)
        Mockito.verify(projectRepository).findByNameAndOwnerId("Test", user)
    }

    @Test
    fun saveProjectThrow() {
        val badObject = ProjectDto(1234, "Test", "", ConfType.java, "",
                ProjectType.USER_PROJECT, "", "1.0.0", emptyList(), emptyList())
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.findByNameAndOwnerId("Test", user)).thenReturn(null)
        Assertions.assertThatExceptionOfType(SourceNotFoundException::class.java)
                .isThrownBy { service.saveProject(USER_ID, badObject) }
                .withMessageContaining("Can not find project by public id")
    }


    @Test
    fun addProject() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.countByOwnerId(user)).thenReturn(5)
        Mockito.`when`(projectRepository.findByNameAndOwnerId("project_name", user)).thenReturn(null)
        Mockito.`when`(idGenerator.nextProjectId()).thenReturn("new_id_generated")
        Mockito.`when`(modelMapper.map(project, ProjectDto::class.java)).thenReturn(projectDto)
        Mockito.`when`(projectRepository.save(Mockito.any(Project::class.java))).thenReturn(project)
        Mockito.`when`(fileService.addFileToProject(project, GeneralPathsConstants.FILE_DEFAULT_CONTENT, project.name))
                .thenReturn(File())

        service.addProject(USER_ID, "project_name")

        Mockito.verify(userService).defineUser(user.clientId!!)
        Mockito.verify(projectRepository).findByNameAndOwnerId("project_name", user)
        Mockito.verify(projectRepository).countByOwnerId(user)
        Mockito.verify(fileService).addFileToProject(project, GeneralPathsConstants.FILE_DEFAULT_CONTENT, project.name)
    }

    @Test
    fun addProjectThrow() {
        Mockito.`when`(userService.defineUser(USER_ID)).thenReturn(user)
        Mockito.`when`(projectRepository.countByOwnerId(user)).thenReturn(101)

        Assertions.assertThatExceptionOfType(ValidationException::class.java)
                .isThrownBy { service.addProject(USER_ID, "project_name_big") }
                .withMessageContaining("You can't save more than 100 projects")
    }

}