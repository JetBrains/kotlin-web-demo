package web.demo.server.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.filter.CompositeFilter
import web.demo.server.common.ActionPathsConstants
import web.demo.server.common.GeneralPathsConstants
import web.demo.server.common.ProviderPathsConstants
import web.demo.server.configuration.resourses.ClientResources
import javax.servlet.Filter


/**
 * Auth class for getting permit to URLs
 *
 * @author Alexander Prendota on 2/16/18 JetBrains.
 */
@Configuration
@EnableOAuth2Client
class AuthConfiguration : WebSecurityConfigurerAdapter() {


    @Value("\${client.home}")
    lateinit var clientHomeUrl: String

    @Autowired
    @Qualifier("oauth2ClientContext")
    lateinit var oauth2ClientContext: OAuth2ClientContext


    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.antMatcher("/**")
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter::class.java)
                .authorizeRequests()
                .antMatchers("/",
                        "/login**",
                        "/user",
                        "/webjars/**",
                        "${GeneralPathsConstants.API_EDU}${GeneralPathsConstants.COURSE}/**",
                        GeneralPathsConstants.API_FILE,
                        GeneralPathsConstants.API_PROJECT,
                        ActionPathsConstants.RUN_KOTLIN,
                        ActionPathsConstants.COMPLETE_KOTLIN,
                        ActionPathsConstants.HIGHLIGHT,
                        ActionPathsConstants.KOTLIN_SERVER,
                        ActionPathsConstants.CONVERT_TO_KOTLIN,
                        ActionPathsConstants.KOTLIN_VERSIONS)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .logout().logoutSuccessUrl(clientHomeUrl).permitAll().and().csrf().disable()
    }

    /**
     * Getting github auth parameters from configuration file
     */
    @Bean
    @ConfigurationProperties("github")
    fun github(): ClientResources {
        return ClientResources()
    }

    /**
     * Getting facebook auth parameters from configuration file
     */
    @Bean
    @ConfigurationProperties("facebook")
    fun facebook(): ClientResources {
        return ClientResources()
    }

    /**
     * Getting google auth parameters from configuration file
     */
    @Bean
    @ConfigurationProperties("google")
    fun google(): ClientResources {
        return ClientResources()
    }


    /**
     * Getting stepik auth parameters from configuration file
     */
    @Bean
    @ConfigurationProperties("stepik")
    fun stepik(): ClientResources {
        return ClientResources()
    }


    @Bean
    fun oauth2ClientFilterRegistration(
            filter: OAuth2ClientContextFilter): FilterRegistrationBean {
        val registration = FilterRegistrationBean()
        registration.filter = filter
        registration.order = -100
        return registration
    }

    private fun ssoFilter(): Filter {
        val filter = CompositeFilter()
        val filters = ArrayList<Filter>()
        filters.add(ssoFilter(facebook(), ProviderPathsConstants.FACEBOOK))
        filters.add(ssoFilter(github(), ProviderPathsConstants.GITHUB))
        filters.add(ssoFilter(google(), ProviderPathsConstants.GOOGLE))
        filters.add(ssoFilter(stepik(), ProviderPathsConstants.STEPIK))
        filter.setFilters(filters)
        return filter
    }

    private fun ssoFilter(client: ClientResources, path: String): Filter {
        val filter = OAuth2ClientAuthenticationProcessingFilter(path)
        val template = OAuth2RestTemplate(client.client, oauth2ClientContext)
        filter.setRestTemplate(template)
        val tokenServices = UserInfoTokenServices(
                client.resource.userInfoUri, client.client.clientId)
        tokenServices.setRestTemplate(template)
        filter.setTokenServices(tokenServices)
        filter.setAuthenticationSuccessHandler(AuthenticationSuccessHandlerProvider())
        return filter
    }

}
