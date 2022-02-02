package com.abeade.plugin.fcm.push.model

data class FCMResponse(
    val success: Int,
    val failure: Int,
    val results: List<Message>
)

data class Message(
    val messageId: String,
    val error: String
)
