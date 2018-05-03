package web.demo.server

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import web.demo.server.repository.FileRepository
import web.demo.server.repository.ProjectRepository
import web.demo.server.repository.UserRepository
import web.demo.server.service.api.FileService
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

    @Autowired
    lateinit var fileService: FileService

    @Test
    fun contextLoads() {
    }

}
