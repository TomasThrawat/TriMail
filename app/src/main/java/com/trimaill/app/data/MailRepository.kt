package com.trimaill.app.data

import com.trimaill.app.model.MailAccount
import com.trimaill.app.model.ConnectionState
import com.trimaill.app.model.MailItem
import kotlinx.coroutines.delay

class MailRepository {
    suspend fun connect(slot: Int) {
        delay(650L + slot * 180L)
    }

    fun inbox(accounts: List<MailAccount>): List<MailItem> {
        val active = accounts.filter { it.email.isNotBlank() && it.state == ConnectionState.CONNECTED }

        if (active.isEmpty()) {
            return listOf(
                MailItem(
                    1,
                    0,
                    "TriMail",
                    "Add your first account",
                    "Connect up to three mailboxes to build your unified inbox.",
                    "Now",
                    true
                ),
                MailItem(
                    2,
                    0,
                    "TriMail",
                    "Material You interface",
                    "Native Material 3 surfaces, shapes, and dynamic color.",
                    "Now",
                    false
                )
            )
        }

        return active.flatMapIndexed { index, account ->
            listOf(
                MailItem(
                    100L + index * 3,
                    account.slot,
                    "${account.provider.label} Team",
                    "Welcome to ${account.provider.label}",
                    "Your connected mailbox is ready in the unified inbox.",
                    "10:${42 - index} AM",
                    index == 0
                ),
                MailItem(
                    101L + index * 3,
                    account.slot,
                    "Project Desk",
                    "Weekly project update",
                    "A compact preview of the latest message in this mailbox.",
                    "9:${18 + index} AM",
                    false
                ),
                MailItem(
                    102L + index * 3,
                    account.slot,
                    "News Digest",
                    "Your daily digest",
                    "Manage all three accounts from one Material You inbox.",
                    "Yesterday",
                    false
                )
            )
        }
    }
}
