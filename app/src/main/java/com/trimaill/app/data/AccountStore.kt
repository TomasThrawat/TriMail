package com.trimaill.app.data

import android.content.Context
import com.trimaill.app.model.MailAccount
import com.trimaill.app.model.Provider

class AccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("trimail_accounts", Context.MODE_PRIVATE)

    fun load(): List<MailAccount> = (0 until 3).map { slot ->
        val email = prefs.getString("email_$slot", "") ?: ""
        val providerName = prefs.getString("provider_$slot", Provider.GOOGLE.name) ?: Provider.GOOGLE.name
        MailAccount(
            slot = slot,
            email = email,
            provider = runCatching { Provider.valueOf(providerName) }.getOrDefault(Provider.GOOGLE)
        )
    }

    fun save(accounts: List<MailAccount>) {
        prefs.edit().apply {
            accounts.forEach { account ->
                putString("email_${account.slot}", account.email)
                putString("provider_${account.slot}", account.provider.name)
            }
        }.apply()
    }
