package web.demo.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class KotlinVersionConfig {
    @JsonProperty("version")
    internal var version: String? = null
    @JsonProperty("latestStable")
    internal var latestStable: Boolean? = null
    internal var build: String? = null
    internal var obsolete: String? = null
    internal var hasScriptJar: Boolean? = null
    internal var stdlibVersion: String? = null
}
