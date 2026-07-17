package com.puregoldgo.core.network

import com.puregoldgo.core.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates a configured Ktor HttpClient for the IBMS backend API.
 * Platform-specific engine is provided via expect/actual.
 * Automatically attaches Bearer token from [TokenStorage] when available.
 */
fun createHttpClient(): HttpClient = HttpClient(createPlatformEngine()) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            },
        )
    }
    install(Auth) {
        bearer {
            loadTokens {
                TokenStorage.getToken()?.let { BearerTokens(accessToken = it, refreshToken = "") }
            }
            refreshTokens {
                TokenStorage.getToken()?.let { BearerTokens(accessToken = it, refreshToken = "") }
            }
        }
    }
    expectSuccess = false
}
