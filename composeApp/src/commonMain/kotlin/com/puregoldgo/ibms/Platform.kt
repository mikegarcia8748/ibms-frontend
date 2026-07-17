package com.puregoldgo.ibms

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
