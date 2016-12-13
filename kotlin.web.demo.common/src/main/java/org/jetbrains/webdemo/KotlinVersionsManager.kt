package org.jetbrains.webdemo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object KotlinVersionsManager {
    val kotlinVersionConfigs: List<KotlinVersionConfig>
    val kotlinVersions: List<String>
    val latestStableVersion: String

    init {
        val configFile = KotlinVersionsManager::class.java.getResourceAsStream("/compilers-config.json")
        kotlinVersionConfigs = jacksonObjectMapper().readValue(configFile)
        kotlinVersions = kotlinVersionConfigs.map { it.version }
        latestStableVersion = kotlinVersionConfigs.find { it.latestStable }?.version ?: throw Exception("Latest stable version not specified");
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class KotlinVersionConfig(
    val version: String,
    val build: String,
    val obsolete: Boolean,
    val latestStable: Boolean = false,
    val hasScriptJar: Boolean = false)