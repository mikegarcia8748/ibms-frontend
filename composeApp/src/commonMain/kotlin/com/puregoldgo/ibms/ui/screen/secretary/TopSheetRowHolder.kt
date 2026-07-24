package com.puregoldgo.ibms.ui.screen.secretary

/**
 * Holds a [TopSheetRow] between the secretary screen and the topsheet detail
 * screen.
 *
 * Routes are `@Serializable` `NavKey`s persisted through saved state, so the
 * full row (with its display strings) must not travel as a route argument.
 * Keeping it in memory means the worst case of a killed process is navigating
 * back to the billing-history list — the correct outcome anyway.
 *
 * Pattern mirrors [com.puregoldgo.ibms.data.auth.ChallengeHolder].
 */
object TopSheetRowHolder {
    var current: TopSheetRow? = null
}
