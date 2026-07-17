package com.puregoldgo.ibms.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Centralized declaration of all navigation routes.
 *
 * Each route is a [NavKey] that the Navigation 3 back stack uses to identify
 * a destination. All routes MUST be declared here — never inline in screens.
 */
@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Login : Route

    @Serializable
    data object ProviderList : Route

    @Serializable
    data class ProviderForm(val providerId: String? = null) : Route
}

/**
 * [SavedStateConfiguration] required by Navigation 3 on non-JVM platforms
 * (iOS, WasmJs). Registers a polymorphic serializer for every [Route] subtype
 * so the back stack can be saved and restored across process death.
 */
val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Login::class, Route.Login.serializer())
            subclass(Route.ProviderList::class, Route.ProviderList.serializer())
            subclass(Route.ProviderForm::class, Route.ProviderForm.serializer())
        }
    }
}
