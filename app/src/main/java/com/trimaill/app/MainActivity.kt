package com.trimaill.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trimaill.app.data.GoogleAuthPolicy
import com.trimaill.app.model.ConnectionState
import com.trimaill.app.model.MailAccount
import com.trimaill.app.model.MailItem
import com.trimaill.app.model.Provider
import com.trimaill.app.ui.MailViewModel

private enum class Screen { INBOX, ACCOUNTS, COMPOSE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TriMailTheme { TriMailApp() } }
    }
}

@Composable
private fun TriMailTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriMailApp(vm: MailViewModel = viewModel()) {
    val accounts by vm.accounts.collectAsState()
    val authMessage by vm.authMessage.collectAsState()
    val googleBusy by vm.googleBusy.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var screen by rememberSaveable { mutableStateOf(Screen.INBOX) }
    var selectedAccount by rememberSaveable { mutableIntStateOf(-1) }

    val connectedCount = accounts.count {
        it.email.isNotBlank() && it.state == ConnectionState.CONNECTED
    }
    val hasAccounts = connectedCount > 0

    LaunchedEffect(authMessage) {
        authMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearAuthMessage()
        }
    }

    LaunchedEffect(connectedCount) {
        if (!hasAccounts && screen == Screen.INBOX) screen = Screen.ACCOUNTS
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                when (screen) {
                    Screen.INBOX -> TopAppBar(
                        title = {
                            Column {
                                Text("Inbox", fontWeight = FontWeight.Bold)
                                Text(
                                    if (hasAccounts) connectedCount.toString() + " accounts connected"
                                    else "Set up your mailboxes",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { screen = Screen.ACCOUNTS }) {
                                Icon(Icons.Default.Menu, contentDescription = "Accounts")
                            }
                        },
                        actions = {
                            IconButton(onClick = { screen = Screen.COMPOSE }) {
                                Icon(Icons.Default.Edit, contentDescription = "Compose")
                            }
                        }
                    )
                    Screen.ACCOUNTS -> TopAppBar(
                        title = { Text("Mailboxes", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            if (hasAccounts) {
                                IconButton(onClick = { screen = Screen.INBOX }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        }
                    )
                    Screen.COMPOSE -> TopAppBar(
                        title = { Text("Compose", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { screen = Screen.INBOX }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (screen != Screen.COMPOSE) {
                    NavigationBar(Modifier.navigationBarsPadding()) {
                        NavigationBarItem(
                            selected = screen == Screen.INBOX,
                            onClick = { screen = Screen.INBOX },
                            icon = { Icon(Icons.Default.Inbox, contentDescription = null) },
                            label = { Text("Inbox") }
                        )
                        NavigationBarItem(
                            selected = screen == Screen.ACCOUNTS,
                            onClick = { screen = Screen.ACCOUNTS },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            label = { Text("Accounts") }
                        )
                    }
                }
            }
        ) { padding ->
            when (screen) {
                Screen.INBOX -> InboxScreen(
                    padding = padding,
                    accounts = accounts,
                    selectedAccount = selectedAccount,
                    onSelect = { selectedAccount = it },
                    mails = vm.inbox().filter {
                        selectedAccount == -1 || it.accountSlot == selectedAccount
                    }
                )
                Screen.ACCOUNTS -> AccountsScreen(
                    padding = padding,
                    accounts = accounts,
                    googleBusy = googleBusy,
                    onUpdate = vm::updateAccount,
                    onConnectGoogle = vm::connectGoogle,
                    onConnectAll = vm::connectAll
                )
                Screen.COMPOSE -> ComposeScreen(
                    padding = padding,
                    accounts = accounts.filter {
                        it.email.isNotBlank() && it.state == ConnectionState.CONNECTED
                    },
                    initialAccount = accounts.firstOrNull { it.email.isNotBlank() }?.slot ?: -1,
                    onDone = { screen = Screen.INBOX }
                )
            }
        }
    }
}

@Composable
private fun InboxScreen(
    padding: PaddingValues,
    accounts: List<MailAccount>,
    selectedAccount: Int,
    onSelect: (Int) -> Unit,
    mails: List<MailItem>
) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search mail") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedAccount == -1,
                onClick = { onSelect(-1) },
                label = { Text("All") }
            )
            accounts.filter {
                it.email.isNotBlank() && it.state == ConnectionState.CONNECTED
            }.forEach { account ->
                FilterChip(
                    selected = selectedAccount == account.slot,
                    onClick = { onSelect(account.slot) },
                    label = { Text(account.provider.label) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(mails, key = { it.id }) { mail ->
                MailCard(mail, accounts.getOrNull(mail.accountSlot))
            }
        }
    }
}

@Composable
private fun MailCard(mail: MailItem, account: MailAccount?) {
    val accent = when (account?.provider) {
        Provider.GOOGLE -> MaterialTheme.colorScheme.primaryContainer
        Provider.OUTLOOK -> MaterialTheme.colorScheme.secondaryContainer
        Provider.YAHOO -> MaterialTheme.colorScheme.tertiaryContainer
        null -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier.size(46.dp).clip(MaterialTheme.shapes.large).background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(account?.provider?.letter ?: "T", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        mail.sender,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (mail.unread) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        mail.time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    mail.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (mail.unread) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    mail.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun AccountsScreen(
    padding: PaddingValues,
    accounts: List<MailAccount>,
    googleBusy: Boolean,
    onUpdate: (Int, String, Provider) -> Unit,
    onConnectGoogle: (Int, String) -> Unit,
    onConnectAll: () -> Unit
) {
    Column(
        Modifier.fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Connect three mailboxes together",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Each filled slot starts independently at the same time.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))

        accounts.forEach { account ->
            AccountCard(
                account = account,
                googleBusy = googleBusy,
                onUpdate = onUpdate,
                onConnectGoogle = onConnectGoogle
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = onConnectAll,
            enabled = accounts.any {
                it.email.isNotBlank() &&
                    it.provider != Provider.GOOGLE &&
                    it.state != ConnectionState.CONNECTING
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Connect non-Google accounts")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AccountCard(
    account: MailAccount,
    googleBusy: Boolean,
    onUpdate: (Int, String, Provider) -> Unit,
    onConnectGoogle: (Int, String) -> Unit
) {
    var expanded by rememberSaveable(account.slot) { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(account.provider.letter, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Account " + (account.slot + 1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (account.state) {
                            ConnectionState.CONNECTING -> "Connecting…"
                            ConnectionState.CONNECTED -> "Connected"
                            ConnectionState.EMPTY -> "Ready to connect"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = account.email,
                onValueChange = {
                    onUpdate(
                        account.slot,
                        it,
                        GoogleAuthPolicy.inferProvider(it, account.provider)
                    )
                },
                label = { Text("Email address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Provider.entries.forEach { provider ->
                    FilterChip(
                        selected = account.provider == provider,
                        onClick = { onUpdate(account.slot, account.email, provider) },
                        label = { Text(provider.label) }
                    )
                }
            }

            if (account.provider == Provider.GOOGLE && account.email.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onConnectGoogle(account.slot, account.email) },
                    enabled = !googleBusy && account.state != ConnectionState.CONNECTING,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        if (account.state == ConnectionState.CONNECTING) {
                            "Connecting with Google…"
                        } else {
                            "Continue with Google"
                        }
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Google will verify that the selected Google account matches this email.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Google uses Android Credential Manager. TriMail never stores your Google password.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ComposeScreen(
    padding: PaddingValues,
    accounts: List<MailAccount>,
    initialAccount: Int,
    onDone: () -> Unit
) {
    var selected by rememberSaveable { mutableIntStateOf(initialAccount) }
    var to by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Send from", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            accounts.forEach { account ->
                AssistChip(
                    onClick = { selected = account.slot },
                    label = { Text(account.email) },
                    leadingIcon = {
                        Text(account.provider.letter, fontWeight = FontWeight.Bold)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected == account.slot) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = to,
            onValueChange = { to = it },
            label = { Text("To") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth().height(260.dp),
            minLines = 10
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onDone,
            enabled = selected >= 0 && to.isNotBlank() && subject.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send")
        }
        Spacer(Modifier.height(24.dp))
    }
}
