package com.blindspot.blindspotapi.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@ConfigurationProperties(prefix = "google.places")
data class GooglePlacesProperties(
    val apiKey: String = "",
    val baseUrl: String = "https://places.googleapis.com",
)

@Configuration
@EnableConfigurationProperties(GooglePlacesProperties::class)
class GooglePlacesConfig {

    @Bean
    fun googlePlacesRestClient(properties: GooglePlacesProperties): RestClient =
        RestClient.builder()
            .baseUrl(properties.baseUrl)
            .build()
}
