package web.demo.server.configuration.resourses

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

/**
 * Getting available kotlin versions from application.yml
 *
 * @author Alexander Prendota on 3/6/18 JetBrains.
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "compilers")
class KotlinVersion {

    @NestedConfigurationProperty
    var versions: List<String> = mutableListOf()
    @NestedConfigurationProperty
    var latestStable: String? = null

}