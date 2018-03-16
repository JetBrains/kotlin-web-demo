package web.demo.server.configuration

import org.modelmapper.ModelMapper
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import javax.sql.DataSource


/**
 * Configuration bean of web-demo-server
 *
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
@Configuration
class ServerConfiguration {

    /**
     * Bean for DTO to Entity mapper and back
     *
     * @return [ModelMapper]
     */
    @Bean
    fun modelMapper(): ModelMapper {
        return ModelMapper()
    }

    /**
     * Bean for rest service
     *
     * @return [RestTemplate]
     */
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }


    /**
     * Cors bean configuration
     *
     * @return [CorsFilter]
     */
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOrigin("*")
        config.addAllowedHeader("*")
        config.addAllowedMethod("GET")
        config.addAllowedMethod("PUT")
        config.addAllowedMethod("POST")
        config.addAllowedMethod("DELETE")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }

    /**
     * If your wanna start application not from Docker -> set database environment in application.yml
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource(): DataSource {
        return DataSourceBuilder
                .create()
                .build()
    }
}
