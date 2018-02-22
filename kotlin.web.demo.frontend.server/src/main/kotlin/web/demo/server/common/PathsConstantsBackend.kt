package web.demo.server.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Paths from application.yml to kotlin-compiler
 *
 * @author Alexander Prendota on 2/7/18 JetBrains.
 */
@Component
class PathsConstantsBackend {

    /**
     * Server main path
     */
    @Value("\${backend.url}")
    lateinit var SERVER_PATH: String

    /**
     * Path to convert java to kotlin code on backend
     */
    @Value("\${backend.convert}")
    lateinit var CONVERT_TO_KOTLIN: String

    /**
     * Path for running kotlin code on backend
     */
    @Value("\${backend.run}")
    lateinit var RUN_KOTLIN: String

    /**
     * Path for getting complections on backend
     */
    @Value("\${backend.complete}")
    lateinit var COMPLETE_KOTLIN: String

}
