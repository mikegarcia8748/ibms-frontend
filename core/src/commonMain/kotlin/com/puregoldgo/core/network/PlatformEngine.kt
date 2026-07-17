package com.puregoldgo.core.network

import io.ktor.client.engine.HttpClientEngineFactory

/** Platform-specific HTTP engine factory. */
expect fun createPlatformEngine(): HttpClientEngineFactory<*>
