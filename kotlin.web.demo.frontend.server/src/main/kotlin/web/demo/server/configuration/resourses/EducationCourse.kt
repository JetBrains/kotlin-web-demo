package web.demo.server.configuration.resourses

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

/**
 * @author Alexander Prendota on 3/12/18 JetBrains.
 */
@Component
@ConfigurationProperties(prefix = "education")
class EducationCourse {
    @NestedConfigurationProperty
    var courses: List<String> = mutableListOf()
}