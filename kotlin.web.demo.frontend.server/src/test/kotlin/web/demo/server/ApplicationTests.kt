package web.demo.server

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import web.demo.server.dtos.ProjectDto
import web.demo.server.repository.FileRepository
import web.demo.server.repository.ProjectRepository
import web.demo.server.repository.UserRepository
import web.demo.server.service.api.ProjectService
import web.demo.server.service.impl.IdentifierGeneratorService

@RunWith(SpringRunner::class)
@SpringBootTest
class ApplicationTests {

    @Autowired
    lateinit var rep: UserRepository

    @Autowired
    lateinit var fileRep: FileRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var projectService: ProjectService

    @Autowired
    lateinit var service: IdentifierGeneratorService

    @Test
    fun contextLoads() {
        val user = rep.findOne(1346)

        val prject = ProjectDto(0,"1223", "",
                "java", "", "", "","",
                "1.2.21", listOf(), listOf())

        projectService.saveProject("1667594636639354", prject)
        projectRepository.findByNameAndOwnerId( "My%20program", user)

    }

}
