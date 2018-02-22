package web.demo.server.common

/**
 * Main path constants for server
 *
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
object PathsConstants {
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
     * Path for getting completion of kotlin code
     */
    const val COMPLETE_KOTLIN = "/completeKotlin"
}
