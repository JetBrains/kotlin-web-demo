package web.demo.server.http

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder


/**
 *
 * HTTP Request component
 *
 * @author Alexander Prendota on 2/6/18 JetBrains.
 */
@Component
class HttpWrapper {

    @Autowired
    lateinit var rest: RestTemplate

    /**
     * Method for evaluating HTTP GET request
     *
     * @param url           - string URL
     * @param parameters    - query parameters for request
     * @param headersMap    - headers for request
     * @param typeResponse  - response type
     *
     * @return - [T] object
     */
    fun <T> doGet(url: String,
                  parameters: Map<String, String>,
                  headersMap: Map<String, String>,
                  typeResponse: Class<T>): T {
        val headers = appendHeaders(headersMap)
        val builder = UriComponentsBuilder.fromUriString(url)
        parameters.forEach { builder.queryParam(it.key, it.value) }
        val requestEntity = HttpEntity<String>("", headers)
        val responseEntity = rest.exchange(builder.build().encode("UTF-8").toUri(),
                HttpMethod.GET,
                requestEntity,
                typeResponse)
        return responseEntity.body
    }

    /**
     * Method for evaluating HTTP POST request
     *
     * @param url           - string URL
     * @param body          - string objects for request
     * @param parameters    - query parameters for request
     * @param headersMap    - headers for request
     * @param typeResponse  - response type
     *
     * @return - [T] object
     */
    fun <T> doPost(url: String,
                   parameters: Map<String, String>,
                   headersMap: Map<String, String>,
                   body: String,
                   typeResponse: Class<T>): T {
        val headers = appendHeaders(headersMap)
        val builder = UriComponentsBuilder.fromUriString(url)
        parameters.forEach { builder.queryParam(it.key, it.value) }
        val requestEntity = HttpEntity<String>(body, headers)
        val responseEntity = rest.exchange(builder.build().encode("UTF-8").toUri(),
                HttpMethod.POST,
                requestEntity,
                typeResponse)
        return responseEntity.body
    }

    /**
     * Method for evaluating HTTP GET request to Stepik
     * ids[] - pk in Stepik
     *
     * @see <a href="https://stepik.org/api/docs/">Stepik API</a>
     * @param url           - string URL
     * @param parameters    - query parameters for request
     * @param headersMap    - headers for request
     * @param typeResponse  - response type
     *
     * @return - [T] object
     */
    fun <T> doGetToStepik(url: String,
                          parameters: List<String>,
                          headersMap: Map<String, String>,
                          typeResponse: Class<T>): T {
        val headers = appendHeaders(headersMap)
        val builder = UriComponentsBuilder.fromUriString(url)
        parameters.forEach { builder.queryParam("ids[]", it) }
        val requestEntity = HttpEntity<String>("", headers)
        val responseEntity = rest.exchange(builder.build().encode("UTF-8").toUri(),
                HttpMethod.GET,
                requestEntity,
                typeResponse)
        return responseEntity.body
    }


    /**
     * Insert headers to [HttpHeaders]
     * @param headersMap - map of headers
     *
     * @return [HttpHeaders] object
     */
    private fun appendHeaders(headersMap: Map<String, String>): HttpHeaders {
        val headers = HttpHeaders()
        if (headersMap.isNotEmpty()) {
            headersMap.forEach { (key, value) -> headers[key] = value }
        }
        return headers
    }

}
