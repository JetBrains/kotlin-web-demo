package web.demo.server.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import web.demo.server.entity.File

/**
 * @author Alexander Prendota on 2/8/18 JetBrains.
 */
@Repository
interface FileRepository: CrudRepository<File, Int>