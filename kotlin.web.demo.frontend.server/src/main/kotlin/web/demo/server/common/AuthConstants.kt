package web.demo.server.common

import org.springframework.security.oauth2.provider.OAuth2Authentication

/**
 * Paths to client ID and user name from [OAuth2Authentication]
 */
object AuthConstants {

    /**
     * Google constants
     */
    const val GOOGLE_USER_NAME = "name"
    const val GOOGLE_CLIENT_ID = "sub"

    /**
     * Facebook constants
     */
    const val FACEBOOK_USER_NAME = "name"
    const val FACEBOOK_CLIENT_ID = "id"

    /**
     * Stepik constants
     */
    const val STEPIK_USER_NAME = "full_name"
    const val STEPIK_CLIENT_ID = "id"
}