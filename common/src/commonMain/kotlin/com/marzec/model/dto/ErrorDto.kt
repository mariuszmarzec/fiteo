package com.marzec.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(val reason: String)