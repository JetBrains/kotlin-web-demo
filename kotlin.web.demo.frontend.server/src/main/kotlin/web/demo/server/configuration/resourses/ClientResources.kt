package web.demo.server.configuration.resourses

import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails

/**
 * Getting 'client' and 'resource' from application.yml for authentication
 *
 * @author Alexander Prendota on 2/19/18 JetBrains.
 */
class ClientResources {

    @NestedConfigurationProperty
    var client = AuthorizationCodeResourceDetails()

    @NestedConfigurationProperty
    var resource = ResourceServerProperties()

}
