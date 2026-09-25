package com.trimaill.app.model

enum class Provider(val label: String, val letter: String) {
    GOOGLE("Google", "G"),
    OUTLOOK("Outlook", "O"),
    YAHOO("Yahoo", "Y")
}

enum class ConnectionState { EMPTY, CONFIGURED, CONNECTING, CONNECTED }

data class MailAccount(
    val slot: Int,
    val email: String = "",
    val provider: Provider = Provider.GOOGLE,
    val state: ConnectionState = ConnectionState.EMPTY
) {
    val displayName: String
        get() = email.substringBefore("@").ifBlank { "Account ${slot + 1}" }
}

data class MailItem(
    val id: Long,
    val accountSlot: Int,
    val sender: String,
    val subject: String,
    val preview: String,
    val time: String,
    val unread: Boolean
)
