package com.puregoldgo.ibms.shared.api

import com.puregoldgo.ibms.shared.model.StoreType
import kotlinx.serialization.Serializable

/**
 * Body for `POST /stores`.
 *
 * Only [storeType], [branchCode] and [name] are required. The remaining
 * location fields are collected at registration time but may be filled in
 * later, so the backend treats them as optional.
 */
@Serializable
data class CreateStoreRequest(
    val storeType: StoreType,
    val branchCode: String,
    val name: String,
    val region: String? = null,
    val province: String? = null,
    val city: String? = null,
    val barangay: String? = null,
    val postal: String? = null,
)
