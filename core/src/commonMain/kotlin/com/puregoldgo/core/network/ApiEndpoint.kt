package com.puregoldgo.core.network

sealed class ApiEndpoint(val url: String) {
    object Auth : ApiEndpoint("https://api.github.com")
}
