package web.demo.server.common

/**
 * Main path constants for server
 *
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
object ActionPathsConstants {
    /**
     * Path for getting kotlin version from server
     */
    const val KOTLIN_VERSIONS = "/kotlinVersions"

    /**
     * Path for getting kotlin code from java code
     */
    const val CONVERT_TO_KOTLIN = "/convertToKotlin"

    /**
     * Path for running kotlin code
     */
    const val RUN_KOTLIN = "/runKotlin"

    /**
     * Path for getting highlighting from backend side
     */
    const val HIGHLIGHT = "/highlight"

    /**
     * Path for getting completion of kotlin code
     */
    const val COMPLETE_KOTLIN = "/completeKotlin"

    /**
     * Legacy path for supporting old API for running, getting versions and getting completion
     */
    const val KOTLIN_SERVER = "/kotlinServer"
}
