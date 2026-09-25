package com.trimaill.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trimaill.app.data.AccountStore
import com.trimaill.app.data.GoogleAuthClient
import com.trimaill.app.data.MailRepository
import com.trimaill.app.model.ConnectionState
import com.trimaill.app.model.MailAccount
import com.trimaill.app.model.MailItem
import com.trimaill.app.model.Provider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MailViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AccountStore(app)
    private val repository = MailRepository()
    private val googleAuthClient = GoogleAuthClient(app)

    private val _accounts = MutableStateFlow(store.load())
    val accounts: StateFlow<List<MailAccount>> = _accounts.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _googleBusy = MutableStateFlow(false)
    val googleBusy: StateFlow<Boolean> = _googleBusy.asStateFlow()

    fun updateAccount(slot: Int, email: String, provider: Provider) {
        _accounts.value = _accounts.value.map {
            if (it.slot == slot) {
                it.copy(
                    email = email.trimStart(),
                    provider = provider,
                    state = if (email.isBlank()) {
                        ConnectionState.EMPTY
                    } else {
                        ConnectionState.CONFIGURED
                    }
                )
            } else {
                it
            }
        }
    }

    fun saveAccounts() {
        val normalized = _accounts.value.map { account ->
            when {
                account.email.isBlank() -> account.copy(state = ConnectionState.EMPTY)
                account.state == ConnectionState.CONNECTED -> account
                else -> account.copy(state = ConnectionState.CONFIGURED)
            }
        }
        _accounts.value = normalized
        store.save(normalized)
    }

    fun deleteAccount(slot: Int) {
        val updated = _accounts.value.map {
            if (it.slot == slot) {
                MailAccount(slot = slot)
            } else {
                it
            }
        }
        _accounts.value = updated
        store.save(updated)
    }

    fun connectGoogle(slot: Int, typedEmail: String) {
        val email = typedEmail.trim()
        if (email.isBlank() || _googleBusy.value) return

        saveAccounts()
        _googleBusy.value = true
        _accounts.value = _accounts.value.map {
            if (it.slot == slot) {
                it.copy(state = ConnectionState.CONNECTING)
            } else {
                it
            }
        }

        viewModelScope.launch {
            try {
                googleAuthClient.signIn(email)
                    .onSuccess { profile ->
                        val updated = _accounts.value.map {
                            if (it.slot == slot) {
                                it.copy(
                                    email = profile.email,
                                    provider = Provider.GOOGLE,
                                    state = ConnectionState.CONNECTED
                                )
                            } else {
                                it
                            }
                        }
                        _accounts.value = updated
                        store.save(updated)
                        _authMessage.value = "Google account connected: " + profile.email
                    }
                    .onFailure { error ->
                        _accounts.value = _accounts.value.map {
                            if (it.slot == slot) {
                                it.copy(state = ConnectionState.CONFIGURED)
                            } else {
                                it
                            }
                        }
                        store.save(_accounts.value)
                        _authMessage.value = error.message ?: "Google sign-in failed."
                    }
            } finally {
                _googleBusy.value = false
            }
        }
    }

    fun clearAuthMessage() {
        _authMessage.value = null
    }

    fun connectAll() {
        saveAccounts()
        val current = _accounts.value
        val filled = current.filter {
            it.email.trim().isNotEmpty() &&
                it.provider != Provider.GOOGLE &&
                it.state == ConnectionState.CONFIGURED
        }
        if (filled.isEmpty()) return

        val slots = filled.map { it.slot }.toSet()

        _accounts.value = current.map {
            if (it.slot in slots) {
                it.copy(state = ConnectionState.CONNECTING)
            } else {
                it
            }
        }

        viewModelScope.launch {
            filled.map { account ->
                async { repository.connect(account.slot) }
            }.awaitAll()

            val connected = _accounts.value.map {
                if (it.slot in slots &&
                    it.email.trim().isNotEmpty() &&
                    it.provider != Provider.GOOGLE
                ) {
                    it.copy(state = ConnectionState.CONNECTED)
                } else {
                    it
                }
            }

            _accounts.value = connected
            store.save(connected)
        }
    }

    fun inbox(): List<MailItem> = repository.inbox(_accounts.value)
}
