package com.chichi.project.models

import kotlinx.serialization.Serializable

@Serializable
data class ClothesRequest(
    val id: Int? = null,
    val description: String,
    val size: String,
    val quantity: String,
    val requester_email: String,
    val phone_number: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val created_at: String? = null
)
