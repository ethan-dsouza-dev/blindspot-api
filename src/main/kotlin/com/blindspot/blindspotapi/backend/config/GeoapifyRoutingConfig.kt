package com.blindspot.blindspotapi.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@ConfigurationProperties(prefix = "geoapify.routing")
data class GeoapifyRoutingProperties(
    val apiKey: String = "",
    val baseUrl: String = "https://api.geoapify.com",
)

@Configuration
@EnableConfigurationProperties(GeoapifyRoutingProperties::class)
class GeoapifyRoutingConfig {

    @Bean
    fun geoapifyRoutingRestClient(properties: GeoapifyRoutingProperties): RestClient =
        RestClient.builder()
            .baseUrl(properties.baseUrl)
            .build()
}
