package web.demo.server.common

/**
 * Provider URL constants
 * Needed for authorization user by provider
 */
object ProviderPathsConstants {

    /**
     * Google constants
     */
    const val GOOGLE = "/login/google"
    const val GOOGLE_USER_NAME = "name"
    const val GOOGLE_CLIENT_ID = "sub"

    /**
     * Facebook constants
     */
    const val FACEBOOK = "/login/facebook"
    const val FACEBOOK_USER_NAME = "name"
    const val FACEBOOK_CLIENT_ID = "id"

    /**
     * Stepik constants
     */
    const val STEPIK = "/login/stepik"
    const val STEPIK_USER_NAME = "full_name"
    const val STEPIK_CLIENT_ID = "id"

    /**
     * GitHub constants
     */
    const val GITHUB = "/login/github"
    const val GITHUB_USER_NAME = "name"
    const val GITHUB_CLIENT_ID = "id"

}