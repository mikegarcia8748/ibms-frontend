package com.puregoldgo.ibms.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Centralized declaration of all navigation routes.
 *
 * Each route is a [NavKey] that the Navigation 3 back stack uses to identify
 * a destination. All routes MUST be declared here — never inline in screens.
 *
 * Route arguments are serialized into saved state and can outlive the process on
 * disk, so nothing secret belongs in one. That is why [SetPassword] carries no
 * challenge token: the token stays in memory in `ChallengeHolder`, and losing it
 * to a killed process is the correct outcome rather than a bug to work around.
 */
@Serializable
sealed interface Route : NavKey {

    /** Decides between [Login] and the app while a stored session is resumed. */
    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    /** The forced password change. Reads its challenge from `ChallengeHolder`. */
    @Serializable
    data object SetPassword : Route

    /** Authenticated, but with no role yet — or deactivated. */
    @Serializable
    data class NoAccess(val reason: NoAccessReason) : Route

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
            subclass(Route.Splash::class, Route.Splash.serializer())
            subclass(Route.Login::class, Route.Login.serializer())
            subclass(Route.SetPassword::class, Route.SetPassword.serializer())
            subclass(Route.NoAccess::class, Route.NoAccess.serializer())
            subclass(Route.ProviderList::class, Route.ProviderList.serializer())
            subclass(Route.ProviderForm::class, Route.ProviderForm.serializer())
        }
    }
}
