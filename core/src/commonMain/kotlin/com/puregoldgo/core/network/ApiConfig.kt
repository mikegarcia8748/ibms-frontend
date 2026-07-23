package com.puregoldgo.core.network

/**
 * Where the backend lives.
 *
 * The HTTP client has to refresh tokens on its own — before any repository is
 * involved — so the base URL cannot live only in repository constructors. It is
 * settable so a platform entry point can point a build at a deployed backend
 * without a rebuild of this module.
 */
object ApiConfig {
    var baseUrl: String = "http://localhost:8082"

    /**
     * Whether this is a production build.
     *
     * Left `false` by default so development builds get HTTP request/response
     * logging for free; a production platform entry point flips this to `true`
     * before the first API call to silence that logging. Settable for the same
     * reason [baseUrl] is — a build can opt in without a rebuild of this module.
     */
    var isProduction: Boolean = false
}
