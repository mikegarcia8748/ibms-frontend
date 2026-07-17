package com.puregoldgo.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(

    // Generic Type to handle all Http Response from server which contains data...
    @SerialName("data")
    val data: T? = null,

    // Server define message that can be modified real time in production...
    @SerialName("message")
    val message: String? = null,

    // (Expected value: 200..299) Http Status Codes,
    // this will be use for some special operations that requires different UI state
    @SerialName("status")
    val status: Int? = null,

    // (Expected value: 'success'/'error')use to identify if the process on server succeeds
    @SerialName("result")
    val result: String? = null
)
