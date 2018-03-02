package web.demo.server.common

import web.demo.server.entity.File
import web.demo.server.entity.Project

/**
 * General paths for web-demo server
 */
object GeneralPathsConstants {

    /**
     * Path for storage information about user
     */
    const val CURRENT_USER = "userInfo"

    /**
     * Basic operations
     */
    const val EXIST = "exist/"
    const val DELETE = "delete/"
    const val SAVE = "save/"
    const val ADD = "add/"
    const val RENAME = "rename/"
    const val ALL = "all/"

    /**
     * Path constants for operation with [File]
     */
    const val API_FILE = "api/file/"
    const val FILE_DEFAULT_CONTENT = "fun main(args: Array<String>) {\n\n}"

    /**
     * Path constants for operation with [Project]
     */
    const val API_PROJECT = "api/project/"
    const val USER_PROJECT_TYPE = "USER_PROJECT"
    const val DEFAULT_PROJECT_NAME = "Project"

}