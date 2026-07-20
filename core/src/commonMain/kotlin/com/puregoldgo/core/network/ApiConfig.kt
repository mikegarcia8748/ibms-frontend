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
    var baseUrl: String = "http://localhost:8080"
}
