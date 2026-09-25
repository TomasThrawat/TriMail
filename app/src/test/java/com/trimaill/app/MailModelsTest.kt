package com.trimaill.app

import com.trimaill.app.data.GoogleAuthPolicy
import com.trimaill.app.data.MailRepository
import com.trimaill.app.model.ConnectionState
import com.trimaill.app.model.MailAccount
import com.trimaill.app.model.Provider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MailModelsTest {
    @Test
    fun displayNameUsesLocalPart() {
        val account = MailAccount(1, "alex@example.com", Provider.OUTLOOK)
        assertEquals("alex", account.displayName)
    }

    @Test
    fun threeSlotsRemainDistinct() {
        val accounts = (0 until 3).map {
            MailAccount(it, "user" + it + "@example.com", Provider.GOOGLE)
        }
        assertEquals(listOf(0, 1, 2), accounts.map { it.slot })
    }

    @Test
    fun configuredMailboxIsNotShownInInboxUntilConnected() {
        val mails = MailRepository().inbox(
            listOf(
                MailAccount(0, "ready@example.com", Provider.OUTLOOK, ConnectionState.CONFIGURED),
                MailAccount(1, "connected@example.com", Provider.GOOGLE, ConnectionState.CONNECTED)
            )
        )
        assertTrue(mails.isNotEmpty())
        assertTrue(mails.all { it.accountSlot == 1 })
    }

    @Test
    fun emptyMailboxCanBeRepresentedByAnUnfilledSlot() {
        val account = MailAccount(2)
        assertEquals(ConnectionState.EMPTY, account.state)
    }

    @Test
    fun gmailAutomaticallyUsesGoogleProvider() {
        assertEquals(
            Provider.GOOGLE,
            GoogleAuthPolicy.inferProvider("person@gmail.com", Provider.OUTLOOK)
        )
    }

    @Test
    fun nonGmailKeepsSelectedProvider() {
        assertEquals(
            Provider.OUTLOOK,
            GoogleAuthPolicy.inferProvider("person@example.com", Provider.OUTLOOK)
        )
    }

    @Test
    fun googleEmailMatchIsCaseInsensitive() {
        assertTrue(
            GoogleAuthPolicy.matchesEmail("Person@gmail.com", "person@gmail.com")
        )
    }

    @Test
    fun googleEmailMismatchIsRejected() {
        assertFalse(
            GoogleAuthPolicy.matchesEmail("person@gmail.com", "other@gmail.com")
        )
    }
}
