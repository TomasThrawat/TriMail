package com.trimaill.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trimaill.app.data.AccountStore
import com.trimaill.app.data.MailRepository
import com.trimaill.app.model.ConnectionState
import com.trimaill.app.model.MailAccount
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

    private val _accounts = MutableStateFlow(store.load())
    val accounts: StateFlow<List<MailAccount>> = _accounts.asStateFlow()

    fun updateAccount(slot: Int, email: String, provider: Provider) {
        _accounts.value = _accounts.value.map {
            if (it.slot == slot) {
                it.copy(
                    email = email,
                    provider = provider,
                    state = ConnectionState.EMPTY
                )
            } else {
                it
            }
        }
    }

    fun connectAll() {
        val current = _accounts.value
        val filled = current.filter { it.email.trim().isNotEmpty() }
        if (filled.isEmpty()) return

        _accounts.value = current.map {
            if (it.email.trim().isEmpty()) it
            else it.copy(state = ConnectionState.CONNECTING)
        }

        viewModelScope.launch {
            filled.map { account ->
                async { repository.connect(account.slot) }
            }.awaitAll()

            val connected = _accounts.value.map {
                if (it.email.trim().isEmpty()) it
                else it.copy(state = ConnectionState.CONNECTED)
            }

            _accounts.value = connected
            store.save(connected)
        }
    }

    fun inbox(): List<com.trimaill.app.model.MailItem> = repository.inbox(_accounts.value)
}
