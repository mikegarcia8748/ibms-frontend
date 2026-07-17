package com.puregoldgo.ibms.data.api

import io.ktor.client.engine.HttpClientEngineFactory

/** Platform-specific HTTP engine factory. */
expect fun createPlatformEngine(): HttpClientEngineFactory<*>
