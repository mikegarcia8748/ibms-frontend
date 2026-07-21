package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.AuthErrorMapper
import com.puregoldgo.core.network.AuthFailure
import com.puregoldgo.core.network.DomainException
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.core.storage.SessionStore
import com.puregoldgo.ibms.shared.api.ApiEnvelope
import com.puregoldgo.ibms.shared.api.ChangePasswordRequest
import com.puregoldgo.ibms.shared.api.LoginRequest
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.api.RefreshRequest
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.model.wireValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

/** Just enough of the error envelope to classify a failure. */
@Serializable
private data class ErrorBody(val message: String? = null)

/**
 * Ktor-based implementation of [AuthRepository].
 *
 * Takes the *plain* HTTP client rather than the session-aware one: these
 * endpoints either need no bearer at all, or — in the case of the password
 * change — carry a challenge token that the session provider must not replace
 * or try to refresh.
 *
 * Tokens are persisted here, and only when the response actually carries a
 * session. That is the temporary-password rule in one line: a
 * `password_change_required` response leaves the app exactly as unauthenticated
 * as it was before the call.
 */
class KtorAuthRepository(
    private val client: HttpClient,
    private val baseUrl: String = ApiConfig.baseUrl,
) : AuthRepository {

    override fun login(username: String, password: String): Flow<Resource<LoginResponse>> =
        authCall {
            client.post(ApiEndpoint.Login.url(baseUrl)) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username = username, password = password))
            }
        }

    override fun completePasswordChange(
        challengeToken: String,
        newPassword: String,
    ): Flow<Resource<LoginResponse>> = authCall(
        // A 401 here means the challenge died — there is no session yet to lose,
        // so reading it as "wrong credentials" would send the user hunting for a
        // typo that does not exist.
        unauthorizedAs = AuthFailure.ChallengeExpired,
    ) {
        client.post(ApiEndpoint.PasswordChange.url(baseUrl)) {
            header(HttpHeaders.Authorization, "Bearer $challengeToken")
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(newPassword = newPassword))
        }
    }

    override fun refreshSession(): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading)

        val refreshToken = SessionStore.refreshToken()
        if (refreshToken == null) {
            // Nothing stored: a first launch, or a previous sign-out. Not an
            // error worth showing — the caller just renders sign-in.
            emit(error(AuthFailure.SessionExpired, status = 401))
            return@flow
        }

        emit(
            execute(unauthorizedAs = AuthFailure.SessionExpired) {
                client.post(ApiEndpoint.Refresh.url(baseUrl)) {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshRequest(refreshToken = refreshToken))
                }
            },
        )
    }

    override fun signOut() {
        SessionStore.clear()
        CurrentUserStore.clear()
    }

    // region Internals

    private fun authCall(
        unauthorizedAs: AuthFailure? = null,
        request: suspend () -> HttpResponse,
    ): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading)
        emit(execute(unauthorizedAs, request))
    }

    /**
     * Runs one auth request and maps it to a [Resource]. [unauthorizedAs]
     * reclassifies a 401 for endpoints where "wrong username or password" is the
     * wrong reading.
     */
    private suspend fun execute(
        unauthorizedAs: AuthFailure?,
        request: suspend () -> HttpResponse,
    ): Resource<LoginResponse> = try {
        val response = request()
        val status = response.status.value

        if (!response.status.isSuccess()) {
            failure(status, response.errorMessage(), unauthorizedAs)
        } else {
            val envelope: ApiEnvelope<LoginResponse> = response.body()
            val data = envelope.data
            if (envelope.result != "success" || data == null) {
                failure(status, envelope.message, unauthorizedAs)
            } else {
                // The only place tokens are persisted. The profile rides along
                // with them: it is recorded only when a session is actually
                // minted, so holding a temporary password never registers
                // anyone as signed in.
                data.session?.let {
                    SessionStore.save(it.accessToken, it.refreshToken)
                    CurrentUserStore.save(
                        id = data.user.id,
                        name = data.user.name,
                        username = data.user.username,
                        role = data.user.role.wireValue,
                        employeeNumber = data.user.employeeNumber,
                    )
                }
                Resource.Success(data)
            }
        }
    } catch (e: Exception) {
        // The request never got an answer, so there is no status to classify:
        // offline rather than rejected.
        failure(status = 0, serverMessage = e.message, unauthorizedAs = null)
    }

    private fun failure(
        status: Int,
        serverMessage: String?,
        unauthorizedAs: AuthFailure?,
    ): Resource<LoginResponse> {
        val mapped = AuthErrorMapper.map(status, serverMessage)
        val overridden = unauthorizedAs != null && mapped.failure == AuthFailure.InvalidCredentials
        return if (overridden) {
            error(unauthorizedAs, status)
        } else {
            Resource.Error(
                DomainException.AuthException(
                    failure = mapped.failure,
                    message = mapped.message,
                    status = status,
                ),
            )
        }
    }

    private fun error(failure: AuthFailure, status: Int): Resource<LoginResponse> = Resource.Error(
        DomainException.AuthException(
            failure = failure,
            message = AuthErrorMapper.messageFor(failure),
            status = status,
        ),
    )

    private suspend fun HttpResponse.errorMessage(): String? =
        runCatching { body<ErrorBody>().message }.getOrNull()

    // endregion
}
