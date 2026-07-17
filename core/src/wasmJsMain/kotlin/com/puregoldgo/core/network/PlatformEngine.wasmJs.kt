package com.puregoldgo.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = Js
