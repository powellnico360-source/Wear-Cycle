package com.chichi.project.models

import kotlinx.serialization.Serializable

@Serializable
data class CollectionPoint(
    val id: Long? = null,
    val name: String,
    val snippet: String? = null,
    val latitude: Double,
    val longitude: Double,
    val created_at: String? = null
)
