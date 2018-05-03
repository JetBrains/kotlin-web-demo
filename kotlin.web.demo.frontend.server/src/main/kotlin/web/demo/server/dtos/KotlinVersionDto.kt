package web.demo.server.dtos

/**
 * @author Alexander Prendota on 2/6/18 JetBrains.
 */
data class KotlinVersionDto(var version: String? = null,
                            var latestStable: Boolean = false)