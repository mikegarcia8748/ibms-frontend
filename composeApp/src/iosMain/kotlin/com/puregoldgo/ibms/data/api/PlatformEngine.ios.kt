package com.puregoldgo.ibms.data.api

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = Darwin
