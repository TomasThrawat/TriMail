package com.trimaill.app

import com.trimaill.app.model.MailAccount
import com.trimaill.app.model.Provider
import org.junit.Assert.assertEquals
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
            MailAccount(it, "user$it@example.com", Provider.GOOGLE)
        }
        assertEquals(listOf(0, 1, 2), accounts.map { it.slot })
    }
}
