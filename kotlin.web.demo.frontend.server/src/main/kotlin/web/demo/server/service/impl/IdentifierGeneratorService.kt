package web.demo.server.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import web.demo.server.repository.FileRepository
import web.demo.server.repository.ProjectRepository
import java.math.BigInteger
import java.security.SecureRandom

/**
 * Legacy ID generator.
 *
 * @author A. Prendota on 2/28/18 JetBrains.
 * @author S. Atamas   on 6/17/15 JetBrains.
 */
@Component
class IdentifierGeneratorService {

    private val random = SecureRandom()

    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    private fun nextId(): String {
        return BigInteger(130, random).toString(32)
    }

    fun nextProjectId(): String {
        while (true) {
            val id = nextId()
            projectRepository.findByPublicId(id) ?: return id
        }
    }

    fun nextFileId(): String {
        while (true) {
            val id = nextId()
            fileRepository.findByPublicId(id) ?: return id
        }
    }
}