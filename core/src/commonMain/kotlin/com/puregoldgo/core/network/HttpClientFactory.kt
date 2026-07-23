package com.puregoldgo.core.network

import com.puregoldgo.core.storage.SessionStore
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val AUTH_PATH_PREFIX = "/auth/"
private const val REQUEST_TIMEOUT_MS = 30_000L

/** Minimal view of `POST /auth/refresh` — enough to rotate, nothing more. */
@Serializable
private data class RefreshRequestBody(val refreshToken: String)

@Serializable
private data class RefreshEnvelope(val result: String, val data: RefreshData? = null)

@Serializable
private data class RefreshData(val session: RefreshedSession? = null)

@Serializable
private data class RefreshedSession(val accessToken: String, val refreshToken: String)

/**
 * The app's HTTP client.
 *
 * Two rules are enforced here rather than at every call site:
 *
 *  - **Rotation is the client's job.** A 401 triggers one call to `/auth/refresh`,
 *    the rotated pair is stored, and the original request is retried. Ktor
 *    serialises this block, so a burst of parallel 401s produces exactly one
 *    refresh — which matters because the backend revokes the presented refresh
 *    token as it issues the next, and a second concurrent attempt would kill the
 *    session it was trying to save.
 *  - **The bearer never goes near the auth endpoints.** Login and refresh take no session
 *    token, and `/auth/password/change` is authorized by a *challenge* token which
 *    the auth provider must not overwrite — that call sets its own header.
 */
fun createHttpClient(): HttpClient = HttpClient(createPlatformEngine()) {
    installCommon()
    install(Auth) {
        bearer {
            loadTokens {
                SessionStore.accessToken()?.let { access ->
                    BearerTokens(accessToken = access, refreshToken = SessionStore.refreshToken())
                }
            }

            refreshTokens {
                val stored = SessionStore.refreshToken() ?: return@refreshTokens null

                val rotated = runCatching {
                    val response = client.post(ApiEndpoint.Refresh.url(ApiConfig.baseUrl)) {
                        markAsRefreshTokenRequest()
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequestBody(stored))
                    }
                    if (!response.status.isSuccess()) {
                        null
                    } else {
                        response.body<RefreshEnvelope>().takeIf { it.result == "success" }?.data?.session
                    }
                }.getOrNull()

                if (rotated == null) {
                    // Either the token was spent/revoked, or the backend is
                    // unreachable. Both leave us unable to authorize the retry;
                    // drop the session and let the UI ask for a fresh sign-in.
                    SessionStore.clear()
                    SessionEvents.notifyExpired()
                    return@refreshTokens null
                }

                SessionStore.save(
                    accessToken = rotated.accessToken,
                    refreshToken = rotated.refreshToken,
                )
                BearerTokens(accessToken = rotated.accessToken, refreshToken = rotated.refreshToken)
            }

            sendWithoutRequest { request ->
                !request.url.encodedPath.startsWith(AUTH_PATH_PREFIX)
            }
        }
    }
    expectSuccess = false
}

/**
 * A client for the authentication endpoints themselves, deliberately without the
 * Auth plugin.
 *
 * `/auth/password/change` carries a challenge token in its own `Authorization`
 * header — a credential the session provider must neither replace nor try to
 * refresh. Giving those calls a plain client is simpler than teaching one client
 * two contradictory rules, and it keeps a rejected challenge from cascading into
 * a session refresh.
 */
fun createAuthHttpClient(): HttpClient = HttpClient(createPlatformEngine()) {
    installCommon()
    expectSuccess = false
}

private fun HttpClientConfig<*>.installCommon() {
    // Feeds NetworkActivity so the global top progress bar sees every request —
    // installed here rather than per-client so login and refresh count too.
    install(NetworkActivityPlugin)
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            },
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MS
    }
    // Trace every request/response in dev builds; production carries no logging
    // plugin at all. LogLevel.ALL includes bodies and the Authorization header,
    // which is acceptable because this branch never runs in production.
    if (!ApiConfig.isProduction) {
        initNetworkLogging()
        install(Logging) {
            logger = NapierKtorLogger()
            level = LogLevel.ALL
        }
    }
}
